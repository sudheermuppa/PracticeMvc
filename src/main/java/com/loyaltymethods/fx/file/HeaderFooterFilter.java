package com.loyaltymethods.fx.file;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Required;

import com.loyaltymethods.fx.data.IntegrationFileDAO;
import com.loyaltymethods.fx.ex.FXCodedException;

/**
 * Deal with header and footer when we are using prefixes for both or just for the footer.
 * What we do is simply update the CX_FINT_FILE table with any values and we filter out the actual
 * header and footer from getting into the detail table through the writer.
 * 
 * @author Emil
 *
 */
public class HeaderFooterFilter implements ItemProcessor<FieldSet, FieldSet>, LineCallbackHandler {
	Logger log = Logger.getLogger(HeaderFooterFilter.class);
	
	private FlatFileValidator flatFileValidator;
	private Map<String, Map<String,String>> hfMapping;
	private IntegrationFileDAO intFileDAO;
	private LineTokenizer headerTokenizer;

	
	public FieldSet process(FieldSet item) throws Exception {
		
		// only do this for header and footer records - we use additional fields in the header-footer line mapper
		// to indicate header/footer records independent of the REC_TYPE designations

		try {
			// if these are not present, then carry on with processing the line
			// if they are we handle them here and we never send the FieldSet further down the line.
		
			item.readString("HEADER_FLG");
			item.readString("FOOTER_FLG");

		}catch(IllegalArgumentException e) {
			return item;
		}
		
		// just load up all the fields we found in the mapping
		Properties ps = new Properties();
		
		Map<String,Object> out = flatFileValidator.processWithValidations(item, hfMapping);
		log.debug("HF Filter - validated map: " + out);
		// Attempting to insert null values into Properties will through NullPointerException. So remove them...
		if(out.get("ERROR_CODE") == null) {
			out.remove("ERROR_CODE");
		}
		if(out.get("ERROR_DESC") == null) {
			out.remove("ERROR_DESC");
		}		
		ps.putAll(out);
		
		if( ps.containsKey("ERROR_CODE"))
			throw new FXCodedException(ps.get("ERROR_CODE").toString(), "Header/Footer: "+ps.get("ERROR_DESC").toString());

		ps.remove("FILE_NAME");

		log.debug("Updating CX_FINT_FILE: " + ps);
		intFileDAO.updateFile(ps);

		// filter out the record.
		return null;
	}

	// getter/setter
	
	public IntegrationFileDAO getIntFileDAO() {
		return intFileDAO;
	}

	@Required
	public void setIntFileDAO(IntegrationFileDAO intFileDAO) {
		this.intFileDAO = intFileDAO;
	}

	public FlatFileValidator getFlatFileValidator() {
		return flatFileValidator;
	}
	
	@Required
	public void setFlatFileValidator(FlatFileValidator flatFileValidator) {
		this.flatFileValidator = flatFileValidator;
	}

	public Map<String, Map<String, String>> getHfMapping() {
		return hfMapping;
	}
	
	@Required
	public void setHfMapping(Map<String, Map<String, String>> hfMapping) {
		this.hfMapping = hfMapping;
	}

	public void handleLine(String line) {
		 try {
			process(headerTokenizer.tokenize(line));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public LineTokenizer getHeaderTokenizer() {
		return headerTokenizer;
	}

	@Required
	public void setHeaderTokenizer(LineTokenizer headerTokenizer) {
		this.headerTokenizer = headerTokenizer;
	}

}
