package com.loyaltymethods.fx.file;

import java.io.IOException;
import java.io.Writer;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.beans.factory.annotation.Required;

import com.loyaltymethods.fx.data.IntegrationFileDAO;

public class ResponseHeaderFooterCallback implements
		FlatFileFooterCallback, FlatFileHeaderCallback {
	
	Logger log = Logger.getLogger(ResponseHeaderFooterCallback.class);

	private Map<String, String> headerMapping;
	private Map<String, String> footerMapping;
	
	String headerFormat;
	String footerFormat;
	
	String headerFields;
	String footerFields;
	
	String headerPrefix;
	String footerPrefix;
	
	String headerSelect;
	String footerSelect;

	IntegrationFileDAO intFile;

	public void writeHeader(Writer writer) throws IOException {
		
		log.debug("Entering response header callback");
		log.debug("headerFields="+headerFields);
		
		if( headerFields == null || headerFields.trim().equals(""))
			return;
		
		Map<String, Object> record = intFile.read(headerSelect, headerFields);
		log.debug("Response header read: "+record);
		
		StringBuilder buf = new StringBuilder();
		
		if(!headerPrefix.equals("__LM__NO__VALUE__")) {
			log.debug("Appending headerPrefix="+headerPrefix);
			buf.append(headerPrefix);
		}
		
		Formatter fmt = new Formatter(buf, Locale.US);
		
		fmt.format(headerFormat, record.values().toArray());
		log.debug("Formatted response header using "+headerFormat+" resulting in: " + buf.toString());
		writer.write(buf.toString());
	}

	public void writeFooter(Writer writer) throws IOException {
		log.debug("Entered response footer callback with footerFields = "+footerFields);

		if( footerFields == null || footerFields.trim().equals(""))
			return;
		
		Map<String, Object> record = intFile.read(footerSelect, footerFields);
		log.debug("Response footer callback read: "+record);
		
		StringBuilder buf = new StringBuilder();
		
		if(!footerPrefix.equals("__LM__NO__VALUE__")) {
			log.debug("Response footer callback added prefix: "+footerPrefix);
			buf.append(footerPrefix);
		}

		Formatter fmt = new Formatter(buf, Locale.US);
		
		fmt.format(footerFormat, record.values().toArray());
		log.debug("Formatted response footer using "+footerFormat+" resulting in: " + buf.toString());
		writer.write(buf.toString());
		writer.write("\n");
		writer.flush();
	}

	public Map<String, String> getHeaderMapping() {
		return headerMapping;
	}

	public void setHeaderMapping(Map<String, String> headerMapping) {
		this.headerMapping = headerMapping;
	}

	public Map<String, String> getFooterMapping() {
		return footerMapping;
	}

	public void setFooterMapping(Map<String, String> footerMapping) {
		this.footerMapping = footerMapping;
	}

	public IntegrationFileDAO getIntFile() {
		return intFile;
	}

	@Required
	public void setIntFile(IntegrationFileDAO intFile) {
		this.intFile = intFile;
	}

	public String getHeaderFields() {
		return headerFields;
	}

	@Required
	public void setHeaderFields(String headerFields) {
		this.headerFields = headerFields;
	}

	public String getFooterFields() {
		return footerFields;
	}

	@Required
	public void setFooterFields(String footerFields) {
		this.footerFields = footerFields;
	}

	public String getHeaderFormat() {
		return headerFormat;
	}

	@Required
	public void setHeaderFormat(String headerFormat) {
		this.headerFormat = headerFormat;
	}

	public String getFooterFormat() {
		return footerFormat;
	}

	@Required
	public void setFooterFormat(String footerFormat) {
		this.footerFormat = footerFormat;
	}

	public String getHeaderPrefix() {
		return headerPrefix;
	}

	@Required
	public void setHeaderPrefix(String headerPrefix) {
		this.headerPrefix = headerPrefix;
	}

	public String getFooterPrefix() {
		return footerPrefix;
	}

	@Required
	public void setFooterPrefix(String footerPrefix) {
		this.footerPrefix = footerPrefix;
	}

	public String getHeaderSelect() {
		return headerSelect;
	}

	public void setHeaderSelect(String headerSelect) {
		this.headerSelect = headerSelect;
	}

	public String getFooterSelect() {
		return footerSelect;
	}

	public void setFooterSelect(String footerSelect) {
		this.footerSelect = footerSelect;
	}
}
