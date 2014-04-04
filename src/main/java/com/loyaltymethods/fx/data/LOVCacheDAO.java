package com.loyaltymethods.fx.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * This is a cache that holds the list of values for the entire input file.
 * We load up only the types that re mentioned in the validation and then
 * use an in-memory cache to speed up the process.
 * 
 * @author Emil
 *
 */
public class LOVCacheDAO implements InitializingBean {
	Logger log = Logger.getLogger(LOVCacheDAO.class);

	private DataSource sblDS;
	private JdbcTemplate dbt;
	
	private List<String> lovTypes = new ArrayList<String>();
	private List<String> lovFields = new ArrayList<String>();
	
	private boolean isLoaded = false;

	/*
	 * Cache structure - optimal for search of map.get(lovType).get(lovField).get(lovValue)
	 * 
	 * { lovType1: { Field1: { Value1: "x", Value2: "x", Value3: "x" }, 
	 *               Field2: { Value1: "x", Value2: "x", Value3: "x" } },
	 *   lovType2: {...}
	 * }
	 * 
	 */
	private Map<String, Map<String, Map<String, String>>> cache = new HashMap<String, Map<String, Map<String,String>>>();
	
	public void afterPropertiesSet() throws Exception {
		dbt = new JdbcTemplate(sblDS);
	}
	
	public boolean lookup(String lovType, String lovField, String lovValue) {
		if(!isLoaded) {
			log.debug("LOV Cache not loaded - returning true.");
			return true;
		}
		else {
			log.debug("lookup(lovType='"+lovType+"',lovField='"+lovField+"',lovValue='"+lovValue+"'");
			return cache.get(lovType).get(lovField).get(lovValue) != null;
		}
	}
	
	protected String genSQL() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT TYPE, ");
		
		for( String field : lovFields) {
			sql.append(field + ",");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" FROM S_LST_OF_VAL " + 
				   "WHERE TYPE IN ( ");
		for(String lovType : lovTypes) {
			sql.append("'"+lovType+ "',");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(")");
		
		log.debug("Cache SQL: "+sql.toString());
		
		return sql.toString();
	}
	
	/**
	 *  Once all registration is done we load the cache.
	 */
	public void loadCache() {
		if( lovTypes.size() == 0)
			return;
		
		SqlRowSet rs = dbt.queryForRowSet(genSQL());
		
		boolean bHasRecs = rs.first();

		while(bHasRecs) {
			
			Map<String, Map<String,String>> fieldMap = cache.get(rs.getString("TYPE"));
			
			if( fieldMap == null ) {
				fieldMap = new HashMap<String, Map<String,String>> ();
				cache.put(rs.getString("TYPE"), fieldMap);
			}
			
			for( String fieldName : lovFields) {
				Map<String, String> fieldNameMap = fieldMap.get(fieldName);
				
				if( fieldNameMap==null) {
					fieldNameMap = new HashMap<String, String>();
					fieldMap.put(fieldName, fieldNameMap);
				}
				fieldNameMap.put(rs.getString(fieldName), "x");
			}
			bHasRecs = rs.next();
		}
		isLoaded = true;
		log.debug(cache);
	}
	
	/**
	 * A InputFieldMapper while validating will register all the needed LOV types
	 * which are then queried into the cache using loadCache().
	 *  
	 * @param lovType
	 * @param lovField
	 */
	public void registerType(String lovType, String lovField ) {
		if( !lovTypes.contains(lovType) ) {
			lovTypes.add(lovType);
		}
		if( !lovFields.contains(lovField)) {
			lovFields.add(lovField);
		}
	}

	public DataSource getSblDS() {
		return sblDS;
	}

	public void setSblDS(DataSource sblDS) {
		this.sblDS = sblDS;
	}
}
