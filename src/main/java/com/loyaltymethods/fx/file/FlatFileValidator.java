package com.loyaltymethods.fx.file;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.loyaltymethods.fx.data.ErrorDAO;
import com.loyaltymethods.fx.data.LOVCacheDAO;
import com.loyaltymethods.fx.ex.FXCodedException;
import com.loyaltymethods.fx.ex.FXException;
import com.loyaltymethods.fx.ex.FXFieldExceedsLengthException;
import com.loyaltymethods.fx.ex.FXFieldPatternMismatchException;
import com.loyaltymethods.fx.ex.FXInvalidDateException;
import com.loyaltymethods.fx.ex.FXInvalidNumberException;
import com.loyaltymethods.fx.ex.FXLookupFailedException;
import com.loyaltymethods.fx.ex.FXRequiredFieldException;
import com.loyaltymethods.fx.ex.FXUnknownTypeException;

public class FlatFileValidator implements ItemProcessor<FieldSet, Map<String,Object>>, InitializingBean {
	Logger log = Logger.getLogger(FlatFileValidator.class);
	
	private String rerun;
	private String timeZone;
	
	private final static String DEFAULT_DATE_PATTERN = "MM/dd/yyyy HH:mm:ss";
	
	private DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
	{
		dateFormat.setLenient(false);
	}
	private NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
	
	private boolean isTyped = false;
	private boolean isValidating = true;

	// Mappings are supplied as meta-data, for example:
	//
	// <map>
	//   <entry key="FIELD_NAME">
	//      <map>
	//        <entry key="Type" value="Date"/>
	//        <entry key="Format" value="YYYYMMDD"/>
	//      </map>
	// </map>
	
	private Map<String, Map<String, String>> validations;
	private LOVCacheDAO lovCache;
	private ErrorDAO errors;
	
	// this can be used to validate with other 

	public Map<String, Object> processWithValidations(FieldSet fieldSet, Map<String,Map<String,String>> valMap) 
		throws Exception {
		
		Map<String, Object> out = new TreeMap<String,Object>();
		// Set values for ERROR_DESC, f to null. Later in this method if there is an exception then the values will be overridden.
		out.put("ERROR_DESC", null);
		out.put("ERROR_CODE", null);
		
		log.debug("Typed Validator:"+isTyped);
		
		for( String fieldName : fieldSet.getNames()) {
			
			log.debug("Validating: "+fieldName + " --> "+valMap.get(fieldName));
			
			// don't validate ROW_ID which is used to update error status during re-validation
			if( fieldName.equals("ROW_ID")) {
				out.put(fieldName, fieldSet.readString(fieldName));
				continue;
			}

			// shut down these moronic fields
			if( fieldName.equals("HEADER_FLG") || fieldName.equals("FOOTER_FLG"))
				continue;
			
			// remove all skip fields from the map that is going to the writer - this is only a problem while
			// reading from the text file. On a revalidation run, we only query the non-skip fields from dtlDBFields in the properties file.
			
			
			if( valMap.get(fieldName).get("Skip") != null && 
					valMap.get(fieldName).get("Skip").trim().toLowerCase().equals("true")) {
				continue;
			}
			
			// even if a 'system' field was mapped in the file somehow, skip it.
			
			if( valMap.get(fieldName).get("System") != null )
				continue;
			
			try {
				Map<String, String> vals = valMap.get(fieldName);
				
				if(vals == null)
					// no validations supplied at all for the field.
					throw new FXCodedException("SBL-FINT-0015", errors.msg("SBL-FINT-0015",fieldName));
				
				if( vals.get("Type") == null ) {
					// this is fatal - the type is not supplied.
					throw new FXCodedException("SBL-FINT-0016", errors.msg("SBL-FINT-0016",fieldName));
				}
		
				// take care of defaulting first at the string level, no type safety required here.
				String readString = defaultValue(vals.get("Default"), fieldSet.readString(fieldName));

				// check for required
				validateRequired(vals.get("Required"), fieldName, readString);

				if( vals.get("Type").equals("Character") ) {
					if(!isTyped)
						out.put(fieldName,  readString);
					String strVal;
					if(isValidating) {
						strVal = validateString(vals, fieldName, readString);
						if(isTyped)
							out.put(fieldName, strVal);
					}
				} else if (vals.get("Type").equals("Number")) {
					if(!isTyped)
						out.put(fieldName,  readString);
					Double dVal;
					if(isValidating ) {
						dVal = validateNumber(vals, fieldName, readString);
						// numbers are not standardized - unless it is something weird it should work.
						if(isTyped) 
							out.put(fieldName, dVal); 
					}
						
				} else if (vals.get("Type").equals("Date")) {
					if(!isTyped)
						out.put(fieldName,  readString);
					Date dtVal;
					if(isValidating) {
						dtVal = validateDate(vals, fieldName, readString);
						// standardize the format: we need this, to enable default conversions in EIM staging
						log.debug("Putting standard date in: "+dateFormat.format(dtVal));
						out.put(fieldName, dateFormat.format(dtVal));
						if(isTyped)
							out.put(fieldName, dtVal);
					}
				} else {
					// this is fatal - we can not determine the type
					throw new FXUnknownTypeException("Field type '" + vals.get("Type") + "' for field '"+
														fieldName+"' is not valid. Valid types are Character, Number or Date.");

				}
			} catch(FXCodedException ex) {
				out.put("ERROR_DESC", ex.getMessage());
				out.put("ERROR_CODE", ex.getCode());
			}
		}	// for all fields in record
			
		return out;
	}
	
	public void checkUnmapped(Map<String, Object> out, Map<String, Map<String, String>> valMap) {
		// ensure we add any unmapped system fields that did not come through the input file
		
		for( String fieldName : valMap.keySet()) {
			if( !out.containsKey(fieldName) && valMap.get(fieldName).get("System") == null ) {
				if( valMap.get(fieldName).get("Default") != null)
					out.put(fieldName, valMap.get(fieldName).get("Default"));
				else if(valMap.get(fieldName).get("Skip") == null 
						|| !valMap.get(fieldName).get("Skip").trim().toLowerCase().equals("true"))	{
					// if the field was not properly defaulted, and it is not a skipped field put it in error status.
					out.put("ERROR_CODE", "SBL-FINT-0014");
					out.put("ERROR_DESC", errors.msg("SBL-FINT-0014",fieldName));
				}
			}
		}
	}
	 
	public Map<String, Object> process(FieldSet fieldSet) throws Exception {
		
		Map<String, Object> out = processWithValidations(fieldSet, validations);
		
		// check unmapped fields without defaults
		
		checkUnmapped(out, validations);
		
		// mark the records as successful or wrong, but not if they are header/footer
		// in a re-run scenario, we don't Prestage because they are already 'Queued' by the UI after correcting them.
			
		log.debug("Rerun: "+rerun);
			
		if(out.get("ERROR_CODE") != null )
			out.put("REC_STATUS", "Error");
		else
			out.put("REC_STATUS", "Prestaged");
		
		log.debug("Mapped record: "+out.toString());
		
		return out;
	}
	
	/**
	 * Trim a value and throw exception if the field is not there.
	 * 
	 * @param string
	 * @param readString
	 */
	private void validateRequired(String val, String fieldName, String readString) throws FXRequiredFieldException {
		if( val == null )
			return;
		else {
			if( val.toLowerCase().equals("true") ) {
				if( readString.trim().equals("")) {
					throw new FXRequiredFieldException("SBL-FINT-0001", errors.msg("SBL-FINT-0001",fieldName));
				}
			}
		}
	}
	
	private String defaultValue(String defaultSetting, String readString) {
		if(defaultSetting == null) {
			return readString==null?"":readString;
		}
		else if( readString == null || readString.trim().equals("")) {
			return defaultSetting;
		}
		else
			return readString;
	}
	
	/**
	 * Validate a date based on format.
	 * 
	 * @param vals
	 * @param fieldName
	 * @param readString
	 * @return
	 */
	private Date validateDate(Map<String, String> vals, String fieldName,
			String readString) throws FXException {
		
		// construct the format

		DateFormat dtf;
		String pattern = DEFAULT_DATE_PATTERN;
		
		if( vals.get("Format") == null ) {
			dtf = dateFormat;
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			log.debug("Field '" + fieldName +"' defaulted date format.");
		}
		else {
			String format;
			String inputTimeZone;
			
			if( vals.get("Format").indexOf(';') >0) {
				format = vals.get("Format").split(";")[0];
				inputTimeZone =  vals.get("Format").split(";")[1];
			}
			else {
				format = vals.get("Format");
				inputTimeZone = timeZone;
			}
			
			dtf = new SimpleDateFormat(format);
			dtf.setTimeZone(TimeZone.getTimeZone(inputTimeZone));
			
			pattern = vals.get("Format");
		}
		
		Date dt;
		try {
			dt = dtf.parse(readString);
		}
		catch(ParseException pe) {
			// it's possible that we standardized it, so try that before we report the actual error
			log.debug("Input date format failed with pattern '"+pattern+"' trying to see if standardized works: "+pe.getMessage());
			try {
				dt = dateFormat.parse(readString);
			}catch(ParseException p) {
				log.error("Tried standardized date '"+readString+"' could not be reparsed with format '"+ ((SimpleDateFormat)dateFormat).toPattern());
				throw new FXInvalidDateException("SBL-FINT-0002", errors.msg("SBL-FINT-0002", fieldName, readString,pattern,pe.getMessage()));
			}
		}
		log.debug("Returning for string: "+readString+", a date of: " + dt.toString());
		return dt;
	}

	/**
	 * Validates number based on format. 
	 * 
	 * @param vals
	 * @param fieldName
	 * @param readString
	 * @return
	 * @throws FXInvalidNumberException 
	 */
	private Double validateNumber(Map<String, String> vals, String fieldName,
			String readString) throws FXInvalidNumberException {
		
		readString = readString.trim();
		
		Double dbl;
		NumberFormat nft;
		String pattern;
		
		// see if we want to do a regular expression - if the format starts with RegEx:
		// in that case, we parse the number as a BigDecimal and validate with the pattern.
		
		if( vals.get("Format") != null && vals.get("Format").startsWith("RegEx:")) {
			String regEx = vals.get("Format").substring("RegEx:".length());

			if( !readString.matches(regEx)) {
				throw new FXInvalidNumberException("SBL-FINT-0003",errors.msg("SBL-FINT-0003",fieldName,readString,regEx));
			}
			else
				return Double.parseDouble(readString);
		}
		
		// if there was no regular expression, try to do it using the DecimalFormat, or whatever format was
		// specified.

		if( vals.get("Format") == null) {
			nft = numberFormat;
			if( numberFormat instanceof DecimalFormat )
				pattern = ((DecimalFormat)numberFormat).toPattern();
			else
				pattern = "default";
		}
		else {
			nft = new DecimalFormat(vals.get("Format"));
			pattern = vals.get("Format");
		}
		
		log.debug("Number pattern: "+pattern);
		
		ParsePosition pp = new ParsePosition(0);
		Number num;
		num = nft.parse(readString,pp);

		if( pp.getIndex() != readString.length()) {
			throw new FXInvalidNumberException("SBL-FINT-0003",errors.msg("SBL-FINT-0003",fieldName,readString,pattern));
		}

		dbl = num.doubleValue();
		
		log.debug("String: " + readString + ", Double: "+dbl);

		return dbl;
	}

	/**
	 * Validates a string field based on a regular expression pattern.
	 * 
	 * @param vals
	 * @param fieldName
	 * @param readString
	 * @return
	 * @throws FXFieldExceedsLengthException 
	 * @throws FXFieldPatternMismatchException 
	 * @throws FXLookupFailedException 
	 */
	private String validateString(Map<String, String> vals, String fieldName,
			String readString) throws FXFieldExceedsLengthException, FXFieldPatternMismatchException, FXLookupFailedException {
		
		// check length first
		String len = vals.get("Length");
		int iLen;
		
		if( len != null ) {
			iLen = Integer.parseInt(len);
			log.debug("Max Length: "+iLen);
			if(iLen < readString.length())
				throw new FXFieldExceedsLengthException("SBL-FINT-0004", errors.msg("SBL-FINT-0004",fieldName, Integer.toString(iLen)));
		}
		
		// check if any pattern is specified on the format
		if( vals.get("Format") != null) {
			Pattern p = Pattern.compile(vals.get("Format"));
			if( !p.matcher(readString).matches() ) {
				throw new FXFieldPatternMismatchException("SBL-FINT-0005", errors.msg("SBL-FINT-0005", fieldName, vals.get("Format")));
			}
		}
		
		// check if need to look up any LOV values
		if( vals.get("Lookup") != null ) {
			String val = vals.get("Lookup");
			
			boolean isValid;
			if( val.indexOf(",") == -1 )
				isValid = lovCache.lookup(val, "VAL", readString);
			else
				isValid = lovCache.lookup(val.split(",")[0].trim(), val.split(",")[1].trim(), readString);
			if(!isValid) {
				throw new FXLookupFailedException("SBL-FINT-0008",errors.msg("SBL-FINT-0008",fieldName, readString, val));
			}
		}
		
		return readString;
	}

	// getters/setters
	
	public Map<String, Map<String, String>> getValidations() {
		return validations;
	}

	@Required
	public void setValidations(Map<String, Map<String, String>> validations) {
		this.validations = validations;
	}

	public boolean isTyped() {
		return isTyped;
	}

	public void setTyped(boolean isTyped) {
		this.isTyped = isTyped;
	}

	public boolean isValidating() {
		return isValidating;
	}

	public void setValidating(boolean isValidating) {
		this.isValidating = isValidating;
	}

	public LOVCacheDAO getLovCache() {
		return lovCache;
	}

	@Required
	public void setLovCache(LOVCacheDAO lovCache) {
		this.lovCache = lovCache;
	}
	
	public void afterPropertiesSet() throws Exception {
		// load up LOV cache if necessary
		if( lovCache != null ) {
			for( Map<String, String> vals : validations.values()) {
				String val = vals.get("Lookup");
				if( val != null) {
					if( val.indexOf(",") == -1 )
						lovCache.registerType(val, "VAL");
					else
						lovCache.registerType(val.split(",")[0].trim(), val.split(",")[1].trim());
				}
			}
			lovCache.loadCache();
		}
	}

	public String getRerun() {
		return rerun;
	}

	public void setRerun(String rerun) {
		this.rerun = rerun;
	}

	public ErrorDAO getErrors() {
		return errors;
	}

	@Required
	public void setErrors(ErrorDAO errors) {
		this.errors = errors;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
}
