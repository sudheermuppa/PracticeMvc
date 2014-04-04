package com.loyaltymethods.fx.data;

import java.io.File;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class IntegrationDAO extends JdbcDaoSupport {
	Logger log = Logger.getLogger(IntegrationDAO.class);
	
	private String timeZone;
	
	public void updateLogFile(String logId, String fileName) {
		// String fileName = new File(((FileAppender)Logger.getRootLogger().getAppender("file")).getFile()).getName();
		String sql = "UPDATE SIEBEL.CX_FINT_INTGLOG SET LOG_FILE='"+fileName+"' WHERE ROW_ID='"+logId+"'";
		log.debug(sql);
		getJdbcTemplate().execute(sql);
	}
	
	// update current PID and session id in the comments of the integration
	public void updateKillInfo(String intId) {
		
		String sql = "UPDATE SIEBEL.CX_FINT_INTGLOG SET COMMENTS='PID="+System.getProperty("pid")+",session=' || "+
				  "(SELECT sid || ',' || serial# "+
				  	 "FROM v$session "+
				    "WHERE sid = (SELECT sid from v$mystat where rownum=1)) WHERE ROW_ID='"+intId+"'";
		
		log.debug(sql);
		getJdbcTemplate().execute(sql);
	}
	// data source already available
	
	/**
	 * Create a log entry for the integration we are currently running.
	 * 
	 * @return
	 */
	public String addLogEntry(String intId, String logStatus, String logEntry) {
		JdbcTemplate dbt = getJdbcTemplate();
		
		// issue a separate query so we can return the rowId
		
		dbt.execute("ALTER SESSION SET TIME_ZONE='"+timeZone+"'");
		
		String rowId = dbt.queryForObject("SELECT LM_GEN_UID() FROM DUAL", String.class);

		dbt.execute("INSERT INTO SIEBEL.CX_FINT_INTGLOG (ROW_ID, " +
														"INTEGRATION_ID,"+
														"CREATED, " + 
														"CREATED_BY, " + 
														"LAST_UPD, " + 
														"LAST_UPD_BY, "+ 
														"LOG_DT, STATUS," + 
														"LOG_TEXT )"+
												" VALUES( '"+rowId+"',"+
														" '"+intId+"',"+
														" CURRENT_DATE,"+
														" '0-1',"+
														" CURRENT_DATE,"+
														" '0-1',"+
														" CURRENT_DATE,"+
														" '"+logStatus+"',"+
														" '"+logEntry.substring(0,Math.min(logEntry.length(),249))+"')");
		

		if( logStatus.equals("Error")) {
			log.debug(logEntry);
		}
		return rowId;
	}
	
	public void changeLogEntry(String rowId, String logStatus, String logEntry) {
		JdbcTemplate dbt = getJdbcTemplate();

		dbt.execute("ALTER SESSION SET TIME_ZONE='"+timeZone+"'");

		dbt.execute("UPDATE SIEBEL.CX_FINT_INTGLOG SET LOG_DT=CURRENT_DATE, STATUS='"+logStatus+"',LOG_TEXT='"+logEntry.substring(0,Math.min(logEntry.length(),249))+"' WHERE ROW_ID='"+rowId+"'");
		if( logStatus.equals("Error")) {
			log.debug(logEntry);
		}
	}
	
	public String lookupIntegrationId(String intName) {
		JdbcTemplate dbt = getJdbcTemplate();
		return dbt.queryForObject("SELECT ROW_ID FROM SIEBEL.CX_FINT_INTG WHERE NAME='"+intName+"'", String.class);
	}
	
	public String lookupIntegrationPattern(String intName) {
		return getJdbcTemplate().queryForObject("SELECT FILE_NAME FROM SIEBEL.CX_FINT_INTG WHERE NAME='"+intName+"'", String.class);
	}

	public String lookupIntegrationOwner(String integrationId) {
		return getJdbcTemplate().queryForObject("SELECT OWNER_EMP_ID FROM SIEBEL.CX_FINT_INTG WHERE ROW_ID='"+integrationId+"'", 
												String.class);
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
}