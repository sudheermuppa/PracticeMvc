package com.loyaltymethods.fx.data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.loyaltymethods.fx.ex.FXFileAlreadyProcessedException;

/**
 * Data object to handle updates to the CX_INT_FILE table in Siebel.
 * 
 * @author Emil
 *
 */
public class IntegrationFileDAO implements InitializingBean {
	Logger log = Logger.getLogger(IntegrationFileDAO.class);
	
	// IoC
	private DataSource batchDS;
	private DataSource sblDS;
	
	private String prestageTable;
	private String baseTable;
	private String eimTable;
	private String timeZone;

	// populated externally
	private String fileId;
	
	// populated upon setting the data source
	private JdbcTemplate dbt;

	private String eimRowIdColumn;
	
	public Date currentDate() {
		return new Date();
	}
	
	public void updateEndDate() {
		log.debug("Updating end date on File.");
		Properties ps = new Properties();
		ps.put("END_DT", currentDate());
		this.updateFile(ps);
	}
	
	public void updateLogFile(String fileName) {
		// String fileName = new File(((FileAppender)Logger.getRootLogger().getAppender("file")).getFile()).getName();
		String sql = "UPDATE SIEBEL.CX_FINT_FILE SET LOG_FILE='"+fileName+".log' WHERE ROW_ID = '"+fileId+"'";
		log.debug(sql);
		dbt.execute(sql);
	}

	public Map<String, Object> read(String selectClause, final String fieldList) {
		log.debug("Response H/F SQL: "+"SELECT "+selectClause+" FROM SIEBEL.CX_FINT_FILE WHERE ROW_ID='"+fileId+"'");
		log.debug("Field List: "+fieldList);
		
		return	dbt.queryForObject("SELECT "+selectClause+" FROM SIEBEL.CX_FINT_FILE WHERE ROW_ID='"+fileId+"'", new RowMapper<Map<String,Object>>() {

			public Map<String, Object> mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				
				// LinkedHashMap preserves the order of insertion which is important in this case
				LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
						
				for(String field : fieldList.split("\\s*,\\s*")) {
					map.put(field, rs.getObject(field));
				}
				log.debug("Response H/F SQL Loaded Map: "+map.toString());
				return map;
			}
		});
	}
	
	/**
	 * Determine whether our file has both Accruals and Redemptions so we can intelligently sequence them.
	 * 
	 * @return
	 */
	public boolean hasAccrualAndRedemption() {
		// this is hopefully the fastest way to peek in the file
		
		int i = dbt.queryForInt("SELECT COUNT(*) FROM (SELECT 1 FROM DUAL WHERE EXISTS (SELECT 1 FROM SIEBEL."+prestageTable+" WHERE FILE_ID='"
								+fileId+"' AND TYPE_CD='Accrual'))");
		
		i += dbt.queryForInt("SELECT COUNT(*) FROM (SELECT 1 FROM DUAL WHERE EXISTS (SELECT 1 FROM SIEBEL."+prestageTable+" WHERE FILE_ID='"
								+fileId+"' AND TYPE_CD='Redemption'))"); 
		
		log.debug("File has both Accruals and Redemptions: " + (i==2));

		return i==2;
	}
	
	// Find out how many records in the transaction table are 'Queued'.
	public long getQueuedCount() {
		return dbt.queryForLong("SELECT COUNT(*) FROM "+baseTable+" WHERE STATUS_CD = 'Queued'");
	}

	// Find out how many records in the transaction table are 'Queued'.
	public long getFileQueuedCount() {
		return dbt.queryForLong("SELECT COUNT(*) FROM "+baseTable+" S, "+eimTable+" E WHERE E.FILE_ID='"+
									fileId+"' AND E."+eimRowIdColumn+" = S.ROW_ID AND S.STATUS_CD = 'Queued'");
	}
	
	// filtering applied to distinguish Accrual, Redemption or Both scenario
	// TODO: consider generalization in some way.
	public long getStagedCount(String filter) {
		String sql = "SELECT COUNT(*) FROM SIEBEL."+eimTable+" WHERE IF_ROW_STAT='FOR_IMPORT' AND FILE_ID='"+fileId+"' AND "+filter;
		log.debug(sql);
		return dbt.queryForLong(sql);
	}
	
	public long getErrorCount() {
		return dbt.queryForLong("SELECT COUNT(*) FROM SIEBEL."+prestageTable+" WHERE REC_STATUS='Error' AND FILE_ID='"+fileId+"'");
	}

	public long getProcessedCount() {
		return dbt.queryForLong("SELECT COUNT(*) FROM SIEBEL."+prestageTable+" WHERE REC_STATUS='Processed' AND FILE_ID='"+fileId+"'");
	}
	
	public long getTotalCount() {
		return dbt.queryForLong("SELECT COUNT(*) FROM "+prestageTable+" WHERE FILE_ID='"+fileId+"'");
	}

	public void updateStats() {
		log.debug("Updating stats.");
		Properties ps = new Properties();
		
		ps.put("TOTAL_REC", dbt.queryForLong("SELECT COUNT(*) FROM "+prestageTable+" WHERE FILE_ID='"+fileId+"'"));
		
		ps.put("VALIDATED_REC", dbt.queryForLong("SELECT COUNT(*) FROM "+prestageTable+
												 " WHERE FILE_ID='"+fileId+"' AND REC_STATUS IN ('Prestaged','Loaded','Processed')"));
		
		ps.put("LOADED_REC", dbt.queryForLong("SELECT COUNT(*) FROM "+prestageTable+
												" WHERE REC_STATUS IN('Loaded') AND FILE_ID='"+fileId+"'"));
		
		ps.put("REJECTED_REC", dbt.queryForLong("SELECT COUNT(*) FROM " + prestageTable +
												" WHERE REC_STATUS = 'Error' AND ERROR_CODE='SBL-FINT-0011' AND FILE_ID='"+fileId+"'"));
		
		ps.put("PROCESSED_REC", dbt.queryForLong("SELECT COUNT(*) FROM " + prestageTable +
				" WHERE REC_STATUS = 'Processed' AND FILE_ID='"+fileId+"'"));

		ps.put("QUEUED_REC", dbt.queryForLong("SELECT COUNT(*) FROM "+prestageTable+
												" WHERE REC_STATUS IN ('Queued') AND FILE_ID='"+fileId+"'"));
		
		updateFile(ps);
	}
	
	/**
	 * Check if a file already exists in the CX_FINT_FILE table.
	 * 
	 * @param name	- file name
	 * @return true/false
	 */
	public boolean fileExists(String name) {
		log.debug("Checking duplicate for: "+name);

		return (dbt.queryForInt("SELECT COUNT(*) "+
								  "FROM SIEBEL.CX_FINT_FILE " + 
								 "WHERE FILE_NAME='"+name+"'")>0);
	}
	
	public void updateProcessingStatus( String stage, String status, String comments ) {
		log.debug("Update processing status.");
		Properties ps = new Properties();
		ps.put("STATUS", status);
		ps.put("ERROR_CODE", (comments.equals("")? "" : "SBL-FINT-000"));
		ps.put("ERROR_DESC",comments);
		ps.put("STAGE",stage);
		updateFile(ps);
	}
	
	public void updateFile(Properties fs) {
		updateFile(fs, fileId);
	}
	
	/**
	 * If the file is already been started, then just updated. Check to see that the status is not Complete.
	 * 
	 * @param fs
	 * @param fileId
	 */
	public void updateFile(Properties fs, String fileId) {
		fs.put("LAST_UPD", currentDate());
		fs.put("LAST_UPD_BY", "0-1"); 	// TODO: Put real user here
		
		if( fs.get("ERROR_DESC") != null)
			fs.put("ERROR_DESC", fs.get("ERROR_DESC").toString().substring(0,Math.min(249,fs.get("ERROR_DESC").toString().length())));
		
		log.debug("Updating with: " + fs);
		
		StringBuilder updateSQL = new StringBuilder();
		
		updateSQL.append("UPDATE CX_FINT_FILE SET ");
		
		for( Object key : fs.keySet().toArray()) {
			updateSQL.append(key.toString() + "=?,");
		}
		updateSQL.deleteCharAt(updateSQL.length()-1);
		updateSQL.append(" WHERE ROW_ID='"+fileId+"'");
		
		log.debug("SQL: "+updateSQL.toString());
		log.debug("Parameters: "+fs.values());
		
		dbt.update(updateSQL.toString(), (Object [])fs.values().toArray());
	}
	
	/**
	 * Create a link between the BATCH_JOB_EXECUTION and the CX_FINT_FILE records.
	 * 
	 * @param fileId
	 * @param jobId
	 */
	public void updateBatchJobFK(String fileId, long jobId) {
		JdbcTemplate dbt = new JdbcTemplate(batchDS);
		
		log.debug("UPDATE BATCH_JOB_INSTANCE SET FILE_ID = '"+fileId+"' WHERE JOB_INSTANCE_ID = "+jobId);
		dbt.execute("UPDATE BATCH_JOB_INSTANCE SET FILE_ID = '"+fileId+"' WHERE JOB_INSTANCE_ID = "+jobId);
	}
	
	/**
	 * Creates a CX_FINT_FILE record. Adds some defaults and generates the ROW_ID. 
	 * Also updates the BATCH_JOB_INSTANCE table with the ROW_ID to provide linkage.
	 * 
	 * Returns the newly created fileId.
	 * 
	 * @param fs
	 */
	public String insertFile(Properties fs) {
		
		String fileId = dbt.queryForObject("SELECT '1-' || LM_GEN_UID() FROM DUAL", String.class);
		log.debug("Generated ID for file: " + fileId);
		
		fs.put("ROW_ID", fileId);
		fs.put("CREATED", currentDate());
		fs.put("CREATED_BY","0-1"); 	// TODO: Put real user here
		fs.put("LAST_UPD", currentDate());
		fs.put("LAST_UPD_BY", "0-1"); 	// TODO: Put real user here
		fs.put("START_DT", currentDate());
		
		StringBuilder insertSQL = new StringBuilder();
		insertSQL.append("INSERT INTO CX_FINT_FILE ( ");
		
		Object [] arr = fs.keySet().toArray();
		for( int i = 0; i<arr.length; i++ ) {
			insertSQL.append(arr[i].toString()).append(",");
		}
		
		insertSQL.deleteCharAt(insertSQL.length()-1);
		insertSQL.append(") VALUES ( ");
		
		for( int i =0; i<fs.size(); i++ ) {
			insertSQL.append("?,");
		}

		insertSQL.deleteCharAt(insertSQL.length()-1);
		insertSQL.append(")");
		
		log.debug("Insert File SQL: " + insertSQL.toString());
		log.debug("Insert File Parameters: " + fs.toString());
		
		dbt.update(insertSQL.toString(),(Object [])fs.values().toArray());

		return fileId;
	}
	
	public String lookupFileName(String fileId) {
		return dbt.queryForObject("SELECT FILE_NAME FROM SIEBEL.CX_FINT_FILE WHERE ROW_ID = '"+fileId+"'", String.class); 
	}
	
	// get an expression - this is used to help generate more dynamic response
	// file names. The idea is that we allow people to quickly pass an expression through a select
	// statement involving the current file. 
	public String getExpr(String selClause) {
		return dbt.queryForObject("SELECT "+selClause+"FROM SIEBEL.CX_FINT_FILE WHERE ROW_ID = '"+fileId+"'", String.class);
	}
	
	// getter/setter
	
	public DataSource getBatchDS() {
		return batchDS;
	}

	public void setBatchDS(DataSource batchDS) {
		this.batchDS = batchDS;
	}

	public DataSource getSblDS() {
		return sblDS;
	}

	public void setSblDS(DataSource sblDS) {
		this.sblDS = sblDS;
		setDbt(new JdbcTemplate(sblDS));
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getPrestageTable() {
		return prestageTable;
	}

	public void setPrestageTable(String prestageTable) {
		this.prestageTable = prestageTable;
	}

	public String getBaseTable() {
		return baseTable;
	}

	public void setBaseTable(String baseTable) {
		this.baseTable = baseTable;
	}

	public JdbcTemplate getDbt() {
		return dbt;
	}

	public void setDbt(JdbcTemplate dbt) {
		this.dbt = dbt;
	}

	public String getEimTable() {
		return eimTable;
	}

	public void setEimTable(String eimTable) {
		this.eimTable = eimTable;
	}

	public String getEimRowIdColumn() {
		return eimRowIdColumn;
	}

	public void setEimRowIdColumn(String eimRowIdColumn) {
		this.eimRowIdColumn = eimRowIdColumn;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * Get the Integration run id for this file.
	 * @return
	 */
	public String getRunId() {
		return dbt.queryForObject("SELECT RUN_ID FROM SIEBEL.CX_FINT_FILE WHERE ROW_ID='"+fileId+"'", String.class);
	}

	/**
	 * Updates all records in the base table fromStatuses toStatus.
	 * @param fromStatuses - array of statuses to be changed.
	 * @param toStatus - target status to change to
	 */
	public void changeRecordStatus(String[] fromStatuses, String toStatus ) {
		String total = "";

		for(int i = 0; i<fromStatuses.length; i++) {
			total = total + "'"+fromStatuses[i]+ "',";
		}
		
		if(!total.equals("")) {
			total = total.substring(0,total.length()-1);
			log.debug("Threshold Status Change: UPDATE "+prestageTable+" SET STATUS_CD = '"+toStatus+"' WHERE STATUS_CD IN (" + total + ") AND FILE_ID = '"+fileId+"'");
			getDbt().update("UPDATE "+prestageTable+" SET REC_STATUS = '"+toStatus+"' WHERE REC_STATUS IN (" + total + ") AND FILE_ID = '"+fileId+"'");
		}
	}
	
	/**
	 * Delete entire integration file, if it is not already EIM-ed into Siebel.
	 */
	public void purgeFile(String fileId) throws FXFileAlreadyProcessedException {

		// check to see if we have any records that are already in the base table.
		String sql = "SELECT COUNT(1) FROM DUAL WHERE EXISTS ( " +
				"SELECT 1 FROM "+this.prestageTable+" STG," + this.baseTable + " BASE " +
				 "WHERE STG.SBL_ROW_ID = BASE.ROW_ID " +
				 "  AND STG.FILE_ID = '"+fileId+"' )";
		
		log.debug("Purge SQL Check: "+sql);
		int numRec = getDbt().queryForInt(sql);
		
		log.debug("Purge Check Returned: "+numRec);

		// if there are records in the base table, throw an exception
		if( numRec == 1) {
			throw new FXFileAlreadyProcessedException();
		}
		
		// if we are ok to do the delete than proceed with it
		sql = "BEGIN DELETE FROM "+this.prestageTable+" WHERE FILE_ID = '"+fileId+"'; \n" +
					"DELETE FROM CX_FINT_FILE WHERE ROW_ID ='"+fileId+"'; \nCOMMIT;\n END;";

		log.debug("Purge Prestage SQL: "+sql);
		getDbt().update(sql);
	}

	public void afterPropertiesSet() throws Exception {
		getDbt().execute("ALTER SESSION SET TIME_ZONE='"+timeZone+"'");
	}
}
