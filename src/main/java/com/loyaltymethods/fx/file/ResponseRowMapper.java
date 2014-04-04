package com.loyaltymethods.fx.file;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

/**
 * This row mapper is used when we read the response detail fields from 
 * the database. It returns a Map of typed objects.
 * 
 * @author Emil
 *
 */
public class ResponseRowMapper implements RowMapper<Map<String, Object>> {
	Logger log = Logger.getLogger(ResponseRowMapper.class);
	
	private String dtlFields;		// to be loaded from job properties file
	private String dtlPrefix;		// detail prefix - can be empty if not required, but has to be there.
	
	public Map<String, Object> mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		
		String fields = "";
		Map<String,Object> record = new LinkedHashMap<String, Object>();
		
		// check if we need to include a prefix in the response
		if(!dtlPrefix.equals("__LM__NO__VALUE__")) 
			fields = "__LM__REC_TYPE,"+dtlFields;
		else
			fields = dtlFields;

		Object value;

		for( String col : fields.split(",")) {
			if( col.equals("__LM__REC_TYPE") ) 
				value = dtlPrefix;
			else
				value = rs.getObject(col.trim());

			record.put(col.trim(),value);
		}

		log.debug("Row Map: "+ record.toString());
		return record;
	}

	public String getDtlFields() {
		return dtlFields;
	}
	
	@Required
	public void setDtlFields(String dtlFields) {
		this.dtlFields = dtlFields;
	}

	public String getDtlPrefix() {
		return dtlPrefix;
	}

	@Required
	public void setDtlPrefix(String dtlPrefix) {
		this.dtlPrefix = dtlPrefix;
	}
}
