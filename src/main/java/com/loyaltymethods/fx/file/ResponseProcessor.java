package com.loyaltymethods.fx.file;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;

/**
 * Translates error codes to partner ones. 
 * Translates 'Unfixable' -> 'Error' and 'Resolved' -> 'Processed'.
 * Ensures Error/Processed values are dynamic.
 * Ensures defaults are handled correctly.
 * 
 * @author Emil
 *
 */
public class ResponseProcessor implements ItemProcessor<Map<String,Object>, Map<String, Object>> {
	Logger log = Logger.getLogger(ResponseProcessor.class);
	
	/*
	 * Format is:
	 * 	{ "SBL-FINT-???": { Code, Description } }
	 */
	private Map<String, Map<String, String>> transMap;
	
	private String errorStatus;
	private String processedStatus;
	
	public Map<String, Object> process(Map<String,Object> item) throws Exception {
		
		// this simply uses translation meta-data for the integration (if it is defined)
		
		processDefaults(item);
		translateErrors(item);
		translateStatusValues(item);
		
		/*
		String names[] = item.getNames();
		String values[] = item.getValues();
		
		String newCode = null;
		String newDescription = null;
		
		for( String name : names ) {
			if( name.equals("ERROR_CODE") && item.readString("ERROR_CODE") != null ) { 
				Map<String, String> trans = transMap.get(item.readString("ERROR_CODE"));
				if( trans != null ) {
					newCode = (String) trans.keySet().toArray()[0];
					newDescription = trans.get(newCode);
				}
			}
		}
		
		for(int i = 0; i<names.length; i++ ) {
			if( newCode != null) {
				if( names[i].equals("ERROR_CODE")) {
					values[i] = newCode;
				}
				else if (names[i].equals("ERROR_DESC")) {
					values[i] = newDescription;
				}
			}
			// Translate error status 'Unfixable' -> 'Error', 'Resolved' -> 'Processed'
			if (names[i].equals("REC_STATUS")) {
				if( values[i].equals("Unfixable") || values[i].equals("Error") )
					values[i] = this.errorStatus;
				if( values[i].equals("Resolved") || values[i].equals("Processed")) {
					values[i] = this.processedStatus;
				}
			}
		}
		DefaultFieldSet fs = new DefaultFieldSet(values, names);
		
		if( newCode != null) 
			log.debug("Translated Error: "+fs);
		
		return fs;
		*/
		return item;
	}

	/**
	 * Translate the errors if there are any errors in this record and if
	 * there are any translations defined for that error code.
	 * 
	 * @param item
	 */
	private void translateErrors(Map<String, Object> item) {
		if( item.get("ERROR_CODE") != null ) {
			log.debug("Response error code is:" + item.get("ERROR_CODE"));
			
			Map<String, String> trans = transMap.get(item.get("ERROR_CODE"));
			if( trans != null) {
				log.debug("Found translation: "+ trans.toString());
				
				item.put("ERROR_CODE", trans.keySet().toArray()[0]);
				item.put("ERROR_DESC", trans.get(item.get("ERROR_CODE")));
				
				log.debug("Applied translation '"+item.get("ERROR_CODE")+"':'"+item.get("ERROR_DESC"));
			}
		}
	}

	/**
	 * Translates status values from Error, and Processed in FX to 
	 * whatever is defined in the status.record.processed and status.record.error settings.
	 * 
	 * Also automatically moves Unfixable -> Error, Resolved -> Processed
	 * 
	 * @param item
	 */
	private void translateStatusValues(Map<String, Object> item) {
		String recStatus = (String) item.get("REC_STATUS");
		if(  recStatus != null ) {
			log.debug("Response record status = "+ recStatus);
			if( recStatus.equals("Unfixable") || recStatus.equals("Error") ) {
				log.debug("Translating response rror status '"+recStatus+"' to status.record.error='"+this.errorStatus+"'");
				item.put("REC_STATUS",this.errorStatus);
			}
			else if( recStatus.equals("Processed") || recStatus.equals("Resolved")) {
				log.debug("Translating response success status '"+recStatus+"' to status.record.processed='"+this.processedStatus+"'");
				item.put("REC_STATUS", this.processedStatus);
			}
			else
				log.debug("Not translating status '"+recStatus+"'.");
		}
		else
			log.error("Response detail record has no REC_STATUS.");
	}

	private void processDefaults(Map<String, Object> item) {
		// TODO Auto-generated method stub
		
	}

	public Map<String, Map<String, String>> getTransMap() {
		return transMap;
	}

	public void setTransMap(Map<String, Map<String, String>> transMap) {
		this.transMap = transMap;
	}

	public String getErrorStatus() {
		return errorStatus;
	}

	public void setErrorStatus(String errorStatus) {
		this.errorStatus = errorStatus;
	}

	public String getProcessedStatus() {
		return processedStatus;
	}

	public void setProcessedStatus(String processedStatus) {
		this.processedStatus = processedStatus;
	}
}
