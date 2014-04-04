package com.loyaltymethods.fx.file;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

public class RevalidationRowMapper implements RowMapper<FieldSet> {
	Logger log = Logger.getLogger(RevalidationRowMapper.class);
	
	private String dtlFields;		// to be loaded from job properties file
	private String dtlPrefix;		// detail prefix - can be empty if not required, but has to be there.
	
	public FieldSet mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		
		String fields = "";
		
		if(!dtlPrefix.equals("__LM__NO__VALUE__")) 
			fields = "__LM__REC_TYPE,"+dtlFields;
		else
			fields = dtlFields;
		
		String [] names = new String [fields.split(",").length];
		String [] values = new String [names.length];
		
		int i = 0;

		for( String col : fields.split(",")) {
			if( col.equals("__LM__REC_TYPE") ) 
				values[i] = dtlPrefix;
			else
				values[i] = rs.getString(col.trim());	// at this point it's all character data

			names[i] = col.trim();
			i++;
		}
		DefaultFieldSet fs = new DefaultFieldSet(values,names);
		log.debug("Row Map: "+ fs.toString());
		return fs;
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
