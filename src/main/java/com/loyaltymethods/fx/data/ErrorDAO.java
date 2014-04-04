package com.loyaltymethods.fx.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * Deal with Error message lookup and parameter substitution. This mirrors the functionality
 * avaialble through LM_ERROR. However, to speed things up, we load up all the errors in memory.
 * 
 * @author Emil
 *
 */
public class ErrorDAO implements InitializingBean {
	Logger log = Logger.getLogger(ErrorDAO.class);
	
	Map<String, String> cache;		// has Code -> Description pairs.
	DataSource sblDS;				// Siebel data source

	public String msg(String code) {
		if(cache.containsKey(code))
			return cache.get(code);
		else
			return "No error text found for code: "+code;
	}

	public String msg(String code, String param1) {
		return msg(code).replace("%1",param1);
	}

	public String msg(String code, String param1, String param2) {
		return msg(code).replace("%1",param1).replace("%2", param2);
	}

	public String msg(String code, String param1, String param2, String param3) {
		return msg(code).replace("%1",param1).replace("%2", param2).replace("%3", param3);
	}
	
	public String msg(String code, String param1, String param2, String param3, String param4) {
		return msg(code).replace("%1",param1).replace("%2", param2).replace("%3", param3).replace("%4",param4);
	}
	
	public String msg(String code, String param1, String param2, String param3, String param4, String param5) {
		return msg(code).replace("%1",param1).replace("%2", param2).replace("%3", param3).replace("%4",param4).replace("%5",param5);
	}
	
	public void loadCache() {
		JdbcTemplate dbt = new JdbcTemplate(sblDS);
		log.debug("Loading cache with SQL: SELECT ERROR_CD, ERROR_DESC FROM SIEBEL.CX_FINT_ERROR");
		cache = dbt.query("SELECT ERROR_CD, ERROR_DESC FROM SIEBEL.CX_FINT_ERROR", new ResultSetExtractor<Map<String, String>>() {
			public Map<String, String> extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				Map<String, String> map = new HashMap<String, String>();
				
				while(rs.next()) {
					map.put(rs.getString("ERROR_CD"), rs.getString("ERROR_DESC"));
				}
				//log.debug("Loaded Error Map: "+map.toString());
				return map;
			}
		});
	}

	public void afterPropertiesSet() throws Exception {
		loadCache();
	}

	public DataSource getSblDS() {
		return sblDS;
	}

	public void setSblDS(DataSource sblDS) {
		this.sblDS = sblDS;
	}
}
