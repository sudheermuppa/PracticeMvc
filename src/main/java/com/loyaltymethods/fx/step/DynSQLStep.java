package com.loyaltymethods.fx.step;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A step which allows configuring a dynamic SQL with 
 * both dynamic and static template substitution.
 * 
 * The template parameters will start with _LM_<ParamName>.
 * 
 * @author Emil
 *
 */
public class DynSQLStep implements Tasklet {
	Logger log = Logger.getLogger(DynSQLStep.class);
	
	private final String LM_PARAM_PREFIX = "_LM_";
	
	private String template;				// resource name of the template
	private Map<String, String> subs;		// substitution map
	private String sqlText; 				// loaded resource SQL
	private Map<String, String> inParams;	// parameters to the stored proc (name is in the format Name:Type
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate; 
	
	/**
	 * Apply subs to the template. Throw an exception if we have a substitution which does not have
	 * a corresponding element in the template;
	 */
	public void applySubstitutions() throws Exception {
		StringBuffer sql = new StringBuffer();
		
		Pattern p = Pattern.compile(Pattern.quote(LM_PARAM_PREFIX) + "<([^>]+)>");
		Matcher m = p.matcher(sqlText);
		
		while(m.find()) {
			log.debug("matcher: "+m.toString());
			log.debug("SQL Subst: "+m.group(1)+" --> " + subs.get(m.group(1)));
			
			m.appendReplacement(sql, subs.get(m.group(1)));
		}
		m.appendTail(sql);
		log.debug("Expanded SQL: "+ sql.toString());
		sqlText = sql.toString();
	}
	
	/**
	 * Read in the SQL form a classpath resource.
	 * 
	 * @throws Exception
	 */
	public void loadTemplate() throws Exception {
		Scanner fs = new Scanner(this.getClass().getClassLoader().getResourceAsStream(template));
		
		StringBuffer sql = new StringBuffer();
		try {
			while(fs.hasNextLine()) {
				sql.append(fs.nextLine() + "\n");
			}
		}finally {
			fs.close();
		}
		sqlText = sql.toString();
	}
	
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		// load it as late as possible, just in case.
		this.loadTemplate();
		this.applySubstitutions();
		
		log.debug("Starting SQLStep execution: " + chunkContext.getStepContext().getStepName());

		JdbcTemplate dbt = getJdbcTemplate();
		dbt.execute(new CallableStatementCreator() {

			public CallableStatement createCallableStatement(Connection con)
					throws SQLException {
				
				CallableStatement stm = con.prepareCall(sqlText);
				
				// go through parameter setting exercise
				for( String param : inParams.keySet()) {
					String name = param.split(":")[0];
					String type = param.split(":")[1];
					
					if( type.toUpperCase().equals("STRING")) {
						stm.setString(name, inParams.get(param));
						log.debug(template+": "+name+"="+inParams.get(param));
					} else if( type.toUpperCase().equals("NUMBER")) {
						stm.setFloat(name, Float.parseFloat(inParams.get(param)));
					} else if (type.toUpperCase().equals("DATE")) {
						SimpleDateFormat f = new SimpleDateFormat("MM/dd/yyyy kk:mm:ss");
						try {
							stm.setDate(name, (Date) f.parse(inParams.get(param)));
						} catch (ParseException e) {
							log.error(e.toString());
							
							throw new RuntimeException("Date parameter was unparsable: '"+inParams.get(param) + "' using format 'MM/dd/yyyy kk:mm:ss'");
						}
					}
				}
				return stm;
			}
		}, new CallableStatementCallback<Object>() {

			public Object doInCallableStatement(CallableStatement cs)
					throws SQLException, DataAccessException {

				cs.execute();
				return null;
			}
		});
		log.debug("Finished  SQLStep execution: " + chunkContext.getStepContext().getStepName());

		return RepeatStatus.FINISHED;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public Map<String, String> getSubs() {
		return subs;
	}

	public void setSubs(Map<String, String> subs) {
		this.subs = subs;
	}

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

	public Map<String, String> getInParams() {
		return inParams;
	}

	public void setInParams(Map<String, String> inParams) {
		this.inParams = inParams;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
}
