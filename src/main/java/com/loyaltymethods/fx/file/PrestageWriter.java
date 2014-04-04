package com.loyaltymethods.fx.file;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

//import com.loyaltymethods.fx.ex.FXException;

/**
 * This is the writer which inserts records into the target pre-staging table.
 * 
 * @author Emil
 *
 */
public class PrestageWriter implements ItemWriter<Map<String,Object>> {
	
	Logger log = Logger.getLogger(PrestageWriter.class);
	
	private String targetTable;
	private DataSource dataSource;
	private String fileId;
	private String operation;
	
	protected void insert(final List<? extends Map<String,Object>> items) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("INSERT INTO "+targetTable+" ( ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, FILE_ID, ");
		
		// make sure there is something in this list before we build the SQL
		if(items.size() > 0) {
			for( String name : items.get(0).keySet()) {
				sql.append(name+",");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(") VALUES ( LM_GEN_UID(), CURRENT_DATE, '0-1', CURRENT_DATE, '0-1', '"+fileId+"',");
			for( String name : items.get(0).keySet()) {
				String exp = (String) items.get(0).get(name);
				if(exp != null) {
					exp = exp.trim();
					if(!exp.startsWith("Expr:"))
						sql.append("?,");
					else
						sql.append(exp.substring(5)+",");					
				} else {
					sql.append("?,");
				}
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(") ");
			
			BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {

				public int getBatchSize() {
					return items.size();
				}

				public void setValues(PreparedStatement ps, int index)
						throws SQLException {
					
					Map<String, Object> record = items.get(index);
					int i = 1;
				
					for( String name : record.keySet()) {
						String value = (String) record.get(name);
						if(value != null) {
							if(!value.trim().startsWith("Expr:"))
								ps.setObject(i++, record.get(name));							
						} else {
							ps.setNull(i++, Types.VARCHAR);
						}
					}
				}
			};

			JdbcTemplate template = new JdbcTemplate(dataSource);
			template.batchUpdate(sql.toString(), setter);
		}
	}
	
	protected void update(final List<? extends Map<String,Object>> items) {
		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE "+targetTable+" SET LAST_UPD=CURRENT_DATE,");
		
		// make sure there is something in this list before we build the SQL
		if(items.size() > 0) {
			for( String name : items.get(0).keySet()) {
				// by excluding the ROW_ID we are making sure it comes last.
				if( !name.equals("ROW_ID")) {
					// check for Expr: types of fields that have been re-introduced during a correction
					
					String expr = (String)items.get(0).get(name);
					if(expr != null)
						expr = expr.trim();
					else
						expr = "";
					
					if(expr.startsWith("Expr:")) {
						sql.append(name+"="+expr.substring(5)+",");
					}
					else
						sql.append(name+"=?,");
				}
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(" WHERE ROW_ID = ?");
			
			BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
				public int getBatchSize() {
					return items.size();
				}

				public void setValues(PreparedStatement ps, int index)
						throws SQLException {
					
					Map<String, Object> record = items.get(index);
		
					int i = 1;
				
					for( String name : record.keySet()) {
						// by excluding the ROW_ID we are making sure it comes last.
						if(!name.equals("ROW_ID"))
							if(record.get(name) != null && !record.get(name).toString().trim().startsWith("Expr:"))
								ps.setObject(i++, record.get(name));
							else if(record.get(name)==null)
								ps.setObject(i++, null);
					}
					ps.setObject(i++, record.get("ROW_ID"));
				}
			};
			
			log.debug("WRITER: "+sql.toString());
			log.debug("WRITER: "+items.toString());

			JdbcTemplate template = new JdbcTemplate(dataSource);
			template.batchUpdate(sql.toString(), setter);
		}
	}
	
	public void write(final List<? extends Map<String,Object>> items) throws Exception {
		if(operation.equals("INSERT")) {
			insert(items);
		}
		else
			update(items);
	}

	// getter/setter

	public String getTargetTable() {
		return targetTable;
	}

	public void setTargetTable(String targetTable) {
		this.targetTable = targetTable;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
}
