package com.loyaltymethods.fx.meta;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.support.JdbcDaoSupport;


/**
 * Parameter generator - the thing that dumps everything from Parameters into 
 * the batch.properties file.
 * 
 * @author Emil
 */
public class ParamGenerator extends JdbcDaoSupport {
	Logger log = Logger.getLogger(ParamGenerator.class);
	
	PasswordManager passwordManager  = new PasswordManager();
	
	public void genParams() throws Exception {
		
		String metaDir = getJdbcTemplate().queryForObject("SELECT VALUE FROM CX_FINT_SETUP WHERE NAME='batch.conf.path'",String.class);

		log.debug("metaDir="+metaDir);
		
		final Properties batch = new Properties();
		
		String sql = "SELECT ROW_ID, NAME, VALUE FROM CX_FINT_SETUP";
		
		log.debug(sql);
		
		getJdbcTemplate().query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String name = rs.getString("NAME");
				String value = rs.getString("VALUE");
				
				if( name.contains("password") && !value.startsWith("__LM__ENC__:")) {
					try {
						String encVal = "__LM__ENC__:" + passwordManager.encrypt(value);
						batch.put(name, encVal);
						
						// make the update on the database value
						getJdbcTemplate().execute("UPDATE CX_FINT_SETUP SET VALUE='"+encVal+"' WHERE ROW_ID='"+rs.getString("ROW_ID")+"'");
						return;
					}catch(Exception e) {
						log.error("Password encryption failed: "+e.toString());
						batch.put(name, value);
						return;
					}
				}
				
				if( name != null && value != null ) 
					batch.put(name, value);
				else
					log.debug("Skipping "+name+"="+value);
			}
		});
		
		try {
			log.debug("Params: "+batch.toString());
			batch.store(new FileOutputStream(new File(metaDir, "batch.properties")), "FeedXChange Generated File");
		} catch (Exception e) {
			log.error(e.toString());
			throw(e);
		}
	}
}
