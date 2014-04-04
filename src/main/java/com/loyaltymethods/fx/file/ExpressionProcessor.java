package com.loyaltymethods.fx.file;

import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Required;

/**
 * Initially this just processes concatenation of the PRTNR_SYS_KEY column. But we can use 
 * it to process other expressions which can not be shipped to the database.
 * 
 * @author Emil
 *
 */
public class ExpressionProcessor implements ItemProcessor<Map<String,Object>, Map<String,Object>> {
	
	private String userKey;
	
	public Map<String, Object> process(Map<String, Object> item)
			throws Exception {
		
		StringBuffer buf = new StringBuffer();
		
		for(String keyCol : userKey.split("\\s*,\\s*")) {
			if( item.get(keyCol)!=null) {
				buf.append(item.get(keyCol).toString());
			}
		}
		
		item.put("PRTNR_SYS_KEY", buf.toString());
		return item;
	}
	
	public String getUserKey() {
		return userKey;
	}
	
	@Required
	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

}
