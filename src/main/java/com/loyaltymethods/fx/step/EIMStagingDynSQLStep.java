package com.loyaltymethods.fx.step;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class EIMStagingDynSQLStep extends DynSQLStep implements InitializingBean {
	Logger log = Logger.getLogger(EIMStagingDynSQLStep.class);
	
	private Map<String, Map<String, String>> mapping;
	
	void computeParams() {

		// we compute two parameters for the insert
		StringBuffer cols = new StringBuffer();
		StringBuffer vals = new StringBuffer();
		
		log.debug("Computing EIM Parameters for SQL Step");

		for( String name : mapping.keySet()) {
			if( mapping.get(name).containsKey("EIM Mapping")) {
				cols.append(mapping.get(name).get("EIM Mapping").split("\\.")[1]+",\n");
				vals.append(makeValue(name, mapping.get(name))+",\n");
			}
		}
		
		if(cols.length() > 0)
		{ 
			cols.deleteCharAt(cols.length()-2);
			vals.deleteCharAt(vals.length()-2);
		}
		
		getSubs().put("InsertColumns", cols.toString());
		getSubs().put("InsertValues", vals.toString());
	}

	private String makeValue(String name, Map<String, String> map) {

		// if there is an expression set internally for a system field, (such as Expr: NULL) then use it.
		if( map.get("System") != null && map.get("Default").startsWith("Expr:")) 
			return map.get("Default").substring("Expr:".length());
		
		if(map.get("Type").equals("Date")) {
			if( map.get("System") != null )
				return "TO_DATE('"+map.get("Default") + "','MM/DD/YYYY HH24:MI:SS')";
			else
				return "TO_DATE(l_Fint_Txn_Table (i)."+name+",'MM/DD/YYYY HH24:MI:SS')";
		}
		else if(map.get("Type").equals("Number")) {
			if( map.get("System") != null)
				return "TO_NUMBER('"+map.get("Default")+"')";
			else
				return "TO_NUMBER(l_Fint_Txn_Table (i)."+name+")";
		}
		else
			if (map.get("System") != null) 
				return "'"+map.get("Default") +"'";
			else
				return "l_Fint_Txn_Table (i)."+name;
	}

	public Map<String, Map<String, String>> getMapping() {
		return mapping;
	}

	@Required
	public void setMapping(Map<String, Map<String, String>> mapping) {
		this.mapping = mapping;
	}

	public void afterPropertiesSet() throws Exception {
		// if no substitutions were defined, then create them
		if( getSubs() == null) 
			setSubs(new HashMap<String,String>());
		
		computeParams();
	}
}
