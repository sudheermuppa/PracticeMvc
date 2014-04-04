package com.loyaltymethods.fx.meta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

class DetailTemplateHandler implements RowCallbackHandler {
	Logger log = Logger.getLogger(DetailTemplateHandler.class);
	
	protected final StringBuffer hdrFields = new StringBuffer();
	protected final StringBuffer ftrFields = new StringBuffer();
	protected final StringBuffer dtlFields = new StringBuffer();
	
	protected final StringBuffer hdrCols=new StringBuffer();
	protected final StringBuffer ftrCols=new StringBuffer();
	protected final StringBuffer dtlCols=new StringBuffer();
	
	protected String fileFormat;
	protected String fileDelimiter;		// for delimited files only
	
	protected Map<String, Map<String, String>> dtlMappings;
	protected Map<String, Map<String, String>> hfMappings;
	
	protected long dtlCol = 1;		// column counter
	protected long hdrCol = 1;		// column counter
	protected long ftrCol = 1;		// column counter
	
	// The following three attributes are required for writing response in fixed width format.
	protected int currCursorPosOfHdrLine = 1;
	protected int currCursorPosOfDtlLine = 1;
	protected int currCursorPosOfFtrLine = 1;
	
	public DetailTemplateHandler(String fileFormat, String fileDelimiter, Map<String,Map<String,String>> hfMappings, Map<String,Map<String,String>> dtlMappings) {
		this.fileFormat = fileFormat;
		this.hfMappings = hfMappings;
		this.dtlMappings = dtlMappings;
		this.fileDelimiter = fileDelimiter;
	}
	
	// To be overriden for response where we do this using a %s type formatting
	// but in the input it has to be done differently using "start-finish" type strings.

	public int appendFixedDefn(StringBuffer buf, ResultSet rs, int currCursorPosOfLine) throws SQLException {
		// NOTE: The currCursorPosOfLine parameter is used in the method overridden for writing response file.
		if(fileFormat.equals("Fixed")) {
			String start = rs.getString("START_POSTN"); 
			String end = rs.getString("END_POSTN");
			buf.append(start + "-" + end+",");
		}
		return currCursorPosOfLine;
	}
	
	// this creates a synthetic field in the input when there is no indication where
	// in the file the field is coming from (i.e. no sequence and no starting position).
	protected Map<String,String> handleSyntheticField(ResultSet rs) throws SQLException {
		// a field which is not coming from the input has to have a default value

		String type = rs.getString("TYPE");
		String stgColName = rs.getString("STG_COL_NAME");
		
		if( rs.getString("VALUE") == null) {
			log.error("Field '"+stgColName +"' in the "+type+" is not properly defaulted.");
			throw new RuntimeException("Field '"+ stgColName +"' in the '"+type+"' is not properly defaulted. FileFormat="+fileFormat);
		}
		// create the field and set it up with whatever parameters we have
		return setField(new HashMap<String,String>(),rs);
	}

	public void processRow(ResultSet rs) throws SQLException {
		String type = rs.getString("TYPE");
		Map<String, String> field;
		
		String stgColName = rs.getString("STG_COL_NAME");
		
		// handle NULL in SEQ_NUM (or START_POSTN) as fields which need to be synthetically
		// created and defaulted.
		
		String nullCheckCol = "";
		
		if( fileFormat.equals("Fixed"))
			nullCheckCol = "START_POSTN";
		else
			nullCheckCol = "SEQ_NUM";
		
		// a weird way to distinguish a NULL number from a 0
		if( rs.getLong(nullCheckCol) == 0 && rs.wasNull()) {
			field = handleSyntheticField(rs);

			if( type.equals("Detail") )
				dtlMappings.put(stgColName, field);
			else
				hfMappings.put(stgColName, field);
			
			return;
		}
		
		if(type.equals("Header")) {
			// if we skipped any fields, add them in
			if( rs.getLong("SEQ_NUM") > hdrCol) {
				while( hdrCol < rs.getLong("SEQ_NUM")) {
					field = new HashMap<String,String>();
					field.put("Skip","True");
					hdrFields.append("__LM__SKIP_"+hdrCol+",");
					hfMappings.put("__LM__SKIP_"+hdrCol, field);
					hdrCol++;
				}
			}
			hdrCol++;

			field = hfMappings.get(stgColName);
			field = setField(field, rs);
			hdrFields.append(stgColName + ",");

			this.currCursorPosOfHdrLine = appendFixedDefn(hdrCols,rs,currCursorPosOfHdrLine);
			
			hfMappings.put(stgColName,field);
		} else if (type.equals("Trailer")) {
			// if we skipped any fields, add them in
			if( rs.getLong("SEQ_NUM") > ftrCol) {
				while( ftrCol < rs.getLong("SEQ_NUM")) {
					field = new HashMap<String,String>();
					field.put("Skip","True");
					ftrFields.append("__LM__SKIP_"+ftrCol+",");
					hfMappings.put("__LM__SKIP_"+ftrCol, field);
					ftrCol++;
				}
			}

			ftrCol++;

			field = hfMappings.get(stgColName);
			field = setField(field,rs);
			ftrFields.append(stgColName + ",");
			
			this.currCursorPosOfFtrLine = appendFixedDefn(ftrCols,rs,currCursorPosOfFtrLine);

			hfMappings.put(stgColName,field);
			
		} else {
			// if we skipped any fields, add them in
			if( rs.getLong("SEQ_NUM") > dtlCol) {
				while( dtlCol < rs.getLong("SEQ_NUM")) {
					field = new HashMap<String,String>();
					field.put("Skip","True");
					dtlFields.append("__LM__SKIP_"+dtlCol+",");
					dtlMappings.put("__LM__SKIP_"+dtlCol, field);
					dtlCol++;
				}
			}

			dtlCol++;

			field = dtlMappings.get(stgColName);
			field = setField(field,rs);
			dtlFields.append(stgColName + ",");

			this.currCursorPosOfDtlLine = appendFixedDefn(dtlCols,rs,currCursorPosOfDtlLine);

			dtlMappings.put(stgColName,field);
		}
	}
	
	protected Map<String,String> setField(Map<String, String> field, ResultSet rs) throws SQLException {
		if(field == null)
			field = new HashMap<String,String>();
		
		field.put("Type", rs.getString("DATA_TYPE"));
		
		// if this already existed in the System, let's make sure it is no longer a system field
		field.remove("System");
		
		if( rs.getString("VALUE") != null)
			field.put("Default", rs.getString("VALUE"));
		if( rs.getString("FORMAT") != null)
			field.put("Format", rs.getString("FORMAT"));
		if( rs.getString("LOOKUP_TYPE") != null)
			field.put("Lookup", rs.getString("LOOKUP_TYPE"));
		if( rs.getString("LENGTH") != null)
			field.put("Length", rs.getString("LENGTH"));

		// do the EIM processing separately
		setupEIM(field, rs);
		return field;
	}
	
	// setup EIM mapping - this can be a bit tricky depending on what type of field it is, and
	// which section of the template we are processing.
	
	// we are also having this separate so we can skip it in the case where we are mapping response
	// defaults and formatting.
	protected void setupEIM(Map<String, String> field, ResultSet rs) throws SQLException {
		if( rs.getString("TYPE").equals("Detail")) {
			if( rs.getString("EIM_TBL_NAME") != null && rs.getString("EIM_COL_NAME") != null)
				field.put("EIM Mapping",rs.getString("EIM_TBL_NAME")+"."+rs.getString("EIM_COL_NAME"));
			else
				// we are ok for custom columns not to have an EIM mapping
				if(!rs.getString("STG_COL_NAME").contains("CUSTOM_COL") && !rs.getString("STG_COL_NAME").startsWith("X_"))
					throw new RuntimeException("Column '"+rs.getString("STG_COL_NAME") + "' has no EIM mapping specified.");
		}
	}
}

/**
 * Different way of specifying template details for responses.
 * 
 * @author Emil
 *
 */
class RespDetailTemplateHandler extends DetailTemplateHandler {
	Logger log = Logger.getLogger(RespDetailTemplateHandler.class);
	
	// SQL Select clauses to enable Oracle-based defaults
	
	protected final StringBuffer hdrSelect = new StringBuffer();
	protected final StringBuffer ftrSelect = new StringBuffer();
	protected final StringBuffer dtlSelect = new StringBuffer();
	
	public RespDetailTemplateHandler(String fileFormat, String delimiter,
			Map<String, Map<String, String>> hfMappings,
			Map<String, Map<String, String>> dtlMappings,
			String respHeaderPrefix, String respDetailPrefix, String respFooterPrefix) {
		super(fileFormat, delimiter, hfMappings, dtlMappings);
		if(!MetaGenerator.NO_VALUE_STRING.equalsIgnoreCase(respHeaderPrefix)) {
			this.currCursorPosOfHdrLine = this.currCursorPosOfHdrLine + respHeaderPrefix.length();
		}
		if(!MetaGenerator.NO_VALUE_STRING.equalsIgnoreCase(respDetailPrefix)) {
			int respDetailPrefixLength = respDetailPrefix.length();
			this.currCursorPosOfDtlLine = this.currCursorPosOfDtlLine + respDetailPrefixLength;
			this.dtlCols.append("%-"+respDetailPrefixLength+"."+respDetailPrefixLength+"s");
		}
		if(!MetaGenerator.NO_VALUE_STRING.equalsIgnoreCase(respFooterPrefix)) {
			this.currCursorPosOfFtrLine = this.currCursorPosOfFtrLine + respFooterPrefix.length();
		}
	}

	// In a response we always use the Formatter even for delimited response 
	// files because it's simpler to control this from here (I think) and
	// also we can override the FORMAT from here for both delimited and response.
	@Override
	public int appendFixedDefn(StringBuffer buf, ResultSet rs, int currCursorPosOfLine) throws SQLException {
		String format = "";

		if( fileFormat.equals("Fixed")) {
			int start = Integer.parseInt(rs.getString("START_POSTN"));
			int end = Integer.parseInt(rs.getString("END_POSTN"));
			int len = end-start+1;
			/*
			 * Check to see if filler required to be added when generating fixed-width response file
			 */
			if(currCursorPosOfLine < start) {
				// Add Filler with spaces.
				int fillerLength = start - currCursorPosOfLine;
				format = String.format("%"+ fillerLength +"s", " ");
				buf.append(format);
			}
			format = "%-"+len+"."+len+"s";

			if( rs.getString("FORMAT") != null ) {
				format = rs.getString("FORMAT");
			}
			currCursorPosOfLine = end + 1;
		}
		else {
			format = "%s"+this.fileDelimiter;
			if( rs.getString("FORMAT") != null )
				format = rs.getString("FORMAT") + this.fileDelimiter;
		}
		
		buf.append(format);
		return currCursorPosOfLine;
	}
	
	// create select statements for response-generation
	
	@Override
	protected Map<String,String> setField(Map<String, String> field, ResultSet rs) throws SQLException {
		field = super.setField(field, rs);
	
		if( rs.getString("TYPE").equals("Detail") )
			setDefault(field, rs, dtlSelect);
		else if( rs.getString("TYPE").equals("Header")) 
			setDefault(field, rs, hdrSelect);
		else if( rs.getString("TYPE").equals("Trailer"))
			setDefault(field, rs, ftrSelect);
		else
			throw new RuntimeException("Field "+rs.getString("STG_COL_NAME")+" belongs to unknown section of the template:"+rs.getString("TYPE"));
		
		return field;
	}
	
	/**
	 * Determine if we need to do an NVL() on any of the fields to put in defaults. 
	 * 
	 * @param field
	 * @param rs
	 * @param buf
	 */
	private void setDefault(Map<String, String> field, ResultSet rs,
			StringBuffer buf) throws SQLException {
		
		String col = rs.getString("STG_COL_NAME");
		String def = field.get("Default");

		if( def != null ) {
			if(def.trim().startsWith("Expr:")) {
				buf.append("NVL("+col+","+def.substring(def.indexOf("Expr:")+"Expr:".length())+") AS "+ col + ",");
			}
			else if( def.trim().startsWith("ForceExpr:")){
				buf.append(def.substring(def.indexOf("ForceExpr:")+"ForceExpr:".length())+" AS "+ col + ",");
			}
			else {
				buf.append("NVL("+col+",'"+def+"') AS "+ col + ",");
			}
		}
		else
			buf.append(col+",");
	}

	// dummy this out so we don't get gripes about non-eim-d columns
	@Override
	protected void setupEIM(Map<String, String> field, ResultSet rs) throws SQLException {
		// nothing
	}
	
	// throw an exception - response fields can not be created without a sequence or
	// position - makes no sense.
	@Override
	protected Map<String,String> handleSyntheticField(ResultSet rs) throws SQLException {
		throw new RuntimeException("Response field '"+rs.getString("STG_COL_NAME")+"' in the response "+rs.getString("TYPE")+" needs to have a position or sequence in the response file.");
	}
}

/**
 * Generates all the metadata for a transaction import integration.
 * 
 * @author Emil
 *
 */
public class MetaGenerator extends JdbcDaoSupport {
	Logger log = Logger.getLogger(MetaGenerator.class);
	public static final String NO_VALUE_STRING = "__LM__NO__VALUE__";
	
	private Map<String,Map<String, Object>> tableMap;	// utility map that needs to be set in order for this to work.
	private String destFolder; 					// where to put the generated files.
	private EntitySpecifics entitySpecifics;	// policy object or each entity as needed
	
	// --- non-spring variables --
	private Properties props;		// properties file
	private PrintWriter xml; 		// xml file
	
	private HashMap<String, Map<String,String>> dtlMappings;
	private HashMap<String, Map<String,String>> hfMappings;
	
	private HashMap<String, Map<String,String>> dtlRespMappings;
	private HashMap<String, Map<String,String>> hfRespMappings;
	
	private HashMap<String, List<String>> eventMap;
	private HashMap<String, Map<String, String>> errorMap;
	private HashMap<String, Map<String, String>> alertMap;
	
	// some values that are useful in multiple steps
	
	private String respFileFormat;
	private String fileFormat;
	protected String indent = "";									// for pretty printing
	protected String intName;
	
	private String intId;			// integration id

	private String templateRowId;
	private String respTemplateRowId;
	
	@SuppressWarnings("unchecked")
	public void genIntegration(String name) throws Exception {
		try {
			// lookup the integration
			intId = getJdbcTemplate().queryForObject("SELECT ROW_ID FROM CX_FINT_INTG WHERE NAME='"+name+"'", String.class);
			intName = name;
			
			// create the Properties object and xml file. Technically we should have the xml be a dom, but for now we will just write things out.
			
			props = new Properties();
			
			dtlMappings = new HashMap<String, Map<String,String>>();
			hfMappings = new HashMap<String,Map<String,String>>();

			dtlRespMappings = new HashMap<String, Map<String,String>>();
			hfRespMappings = new HashMap<String,Map<String,String>>();

			errorMap = new HashMap<String,Map<String,String>>();
			alertMap = new HashMap<String,Map<String,String>>();
			eventMap = new HashMap<String,List<String>>();
			
			xml = new PrintWriter(new FileWriter(destFolder+"/"+name+".xml", false));
			
			// now handle each of the pieces
			
			processTemplateHeader();
			
			// put in some reasonable defaults - at this point the prestaging table name is known (after processing template header)
			dtlMappings.putAll((Map<? extends String, ? extends Map<String, String>>) tableMap.get(props.get("prestageTable")).get("SysFields"));
			
			log.debug("processRespTemplateHeader();");
			processRespTemplateHeader();
			log.debug("processIntegrationHeader();");
			processIntegrationHeader();
			log.debug("processTemplateDetail();");
			processTemplateDetail();
			log.debug("processRespTemplateDetail();");
			processRespTemplateDetail();
			processUserKey();
			log.debug("processUserKey();");
			processEventMap();
			log.debug("processEventMap();");
			log.debug("processAlertMap();");
			processAlertMap();
			log.debug("processErrorMap();");
			processErrorMap();
			log.debug("processIntegrationParameters();");
			processIntegrationParameters();
			
			// if specifics are defined for the entity in question, then generate them 
			if(tableMap.get(props.get("prestageTable")).get("EntitySpecifics") != null)
				((EntitySpecifics)tableMap.get(props.get("prestageTable")).get("EntitySpecifics")).processEntitySpecifics(this);
			
			// write out the actual files
			
			log.debug("About to save:\n"+props.toString());
			props.store(new FileOutputStream(new File(destFolder, name+".properties")), "This file is auto-generated by FeedXChange");
			
			printXMLHeader(xml);
			push();
			log.debug("Writing dtlMappings="+dtlMappings.toString());
			printMapOfMap(dtlMappings, "dtlMapping", xml);
			log.debug("Writing hfMappings="+hfMappings.toString());
			printMapOfMap(hfMappings, "hfMapping", xml);

			log.debug("Writing dtlRespMappings="+dtlRespMappings.toString());
			printMapOfMap(dtlRespMappings, "dtlRespMapping", xml);
			log.debug("Writing hfRespMappings="+hfRespMappings.toString());
			printMapOfMap(hfRespMappings, "hfRespMapping", xml);

			log.debug("Writing alertMap="+alertMap.toString());
			printMapOfMap(alertMap, "alertMap", xml);
			log.debug("Writing errorMap="+errorMap.toString());
			printMapOfMap(errorMap, "errorMap", xml);
			log.debug("Writing eventMap="+eventMap.toString());
			printMapOfList(eventMap,"eventMap", xml);
			pull();
			printXMLFooter(xml);
			xml.close();
		}
		catch(Exception e) {
			// any exceptions should be logged so that Siebel can display them.
			
			PrintWriter err = new PrintWriter(new FileWriter(destFolder+"/"+name+".err", false));
			err.write(e.toString());
			err.close();
			
			throw e;
		}
	}
	
	// run through the parameters and override any global properties, or 
	// for that matter, any patching needed for the params in general
	
	private void processIntegrationParameters() {
		String sql = "SELECT NAME, VALUE "+
                "FROM CX_FINT_INTGPRM " + 
			    "WHERE INTEGRATION_ID = '"+intId+"'";
	
		log.debug(sql);
		
		getJdbcTemplate().query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				if(rs.getString("NAME") != null && 
					rs.getString("VALUE") != null )
					props.setProperty(rs.getString("NAME"), rs.getString("VALUE"));
				else
					throw new RuntimeException("Invalid integration parameter pair: "+rs.getString("NAME")+" -> "+rs.getString("VALUE"));
			}
		});
	}

	protected String esc(String str) {
		return StringEscapeUtils.escapeXml(str);
	}
	protected void push() {
		indent = indent + "\t";
	}
	
	protected void pull() {
		indent = indent.substring(0,indent.length()-1);
	}
	
	/**
	 * Print a map of maps to a print writer as XML.
	 */
	
	protected void printMapOfMap(Map<String, Map<String,String>> map, String id, PrintWriter pw) {
		
		pw.append(indent+"<util:map id=\""+esc(id)+"\">\n");
		push();
		for(String name : map.keySet()) {
			pw.append(indent+"<entry key=\""+esc(name)+"\">\n");
			push();
			printAnonMap(map.get(name), pw);
			pull();
			pw.append(indent+"</entry>\n");
		}
		pull();
		pw.append(indent+"</util:map>\n");
	}
	
	private void printAnonMap(Map<String, String> map, PrintWriter pw) {
		pw.append(indent+"<map>\n");
		push();
		for(String name : map.keySet()) {
			pw.append(indent+"<entry key=\""+esc(name)+"\" value=\"" + esc(map.get(name))+"\" />\n");
		}
		pull();
		pw.append(indent+"</map>\n");
	}
	
	private void printMapOfList(Map<String, List<String>> map, String id, PrintWriter pw) {
		pw.append(indent+"<util:map id=\""+esc(id)+"\">\n");
		push();
		for(String name : map.keySet()) {
			pw.append(indent+"<entry key=\""+esc(name)+"\">\n");
			push();
			printAnonList(map.get(name), pw);
			pull();
			pw.append(indent+"</entry>\n");
		}
		pull();
		pw.append(indent+"</util:map>\n");
	}
	
	private void printXMLHeader(PrintWriter pw) {
		String hdr = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"+
		"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n"+
			"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
			"\txmlns:util=\"http://www.springframework.org/schema/util\"\n"+
			"\txmlns:batch=\"http://www.springframework.org/schema/batch\"\n"+
			"\txmlns:context=\"http://www.springframework.org/schema/context\"\n"+
			"\txsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\n"+
				"\t\thttp://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd\n"+
				"\t\thttp://www.springframework.org/schema/batch http://www.springframework.org/schema/batch/spring-batch.xsd\n"+
				"\t\thttp://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd\">\n"+
				"\n"+
			"\t<!-- Hertz Accrual Integration Metadata -->\n"+
			"\n"+
			"\t<context:property-placeholder ignore-unresolvable=\"true\" location=\"classpath:"+intName+".properties\" />\n"+
			"\n"+
			"\t <import resource=\"classpath:"+this.getEntitySpecificContextFile()+"\"/>\n";
			
	
		pw.append(hdr);
	}
	
	private void printXMLFooter(PrintWriter pw) {
		pw.append("</beans>");
	}
	
	private void printAnonList(List<String> lst, PrintWriter pw) {
		pw.append(indent+"<list>\n");
		push();
		for( String val : lst) {
			pw.append(indent+"<value><![CDATA["+val+"]]></value>\n");
		}
		pull();
		pw.append(indent+"</list>\n");
	}
	
	/**
	 * Read through the user key fields and store them in the properties
	 */
	private void processUserKey() {
		String sql = "SELECT STG_COL_NAME "+
	                 "FROM CX_FINT_INTGATR I, CX_FINT_TMPLATR A " + 
				    "WHERE INTEGRATION_ID = '"+intId+"' "+
	                  "AND A.ROW_ID=I.ATTRIBUTE_ID "+
				      "ORDER BY SEQ_NUM";
		
		log.debug(sql);
		final StringBuffer userKey = new StringBuffer();
		
		getJdbcTemplate().query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				userKey.append(rs.getString("STG_COL_NAME") + ",");
			}
		});
		
		if(userKey.length()>0)
			userKey.deleteCharAt(userKey.length()-1);
		
		props.put("userKey", userKey.toString());
	}
	
	/**
	 * Populate the eventMap variable to get it ready for writing to metadata.
	 */
	private void processEventMap() {
		String sql = "SELECT * FROM CX_FINT_INTGEVT WHERE INACTIVE='N' AND INTEGRATION_ID = '"+intId+"' ORDER BY EVT_NAME, EVT_SEQ_NUM";
	
		log.debug(sql);
		
		getJdbcTemplate().query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				List<String> lst = eventMap.get(rs.getString("EVT_NAME"));
				if(lst == null) {
					lst = new LinkedList<String>();
					eventMap.put(rs.getString("EVT_NAME"), lst);
				}
				lst.add(rs.getString("EXT_CMD"));
			}
		});
	}
	
	/**
	 * Populate the alertMap from the CX_FINT_INTALT table.
	 */
	private void processAlertMap() {
		String sql = "SELECT * FROM CX_FINT_INTGALT WHERE INTEGRATION_ID = '"+intId+"' ORDER BY NTFY_TYPE";
		
		log.debug(sql);
		
		getJdbcTemplate().query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				Map<String, String> map = alertMap.get(rs.getString("NTFY_TYPE"));
				if( map == null ) {
					map = new HashMap<String,String>();
					alertMap.put(rs.getString("NTFY_TYPE"),map);
				}
				map.put(rs.getString("NAME"),rs.getString("EMAIL_ADDR"));
			}
		});		
	}
	
	/**
	 * Populate the errorMap with error translations from CX_FINT_INTGERR
	 */
	
	private void processErrorMap() {
		String sql = "SELECT * FROM CX_FINT_INTGERR WHERE INTEGRATION_ID = '"+intId+"' ";
		
		log.debug(sql);
		
		getJdbcTemplate().query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				Map<String, String> map = alertMap.get(rs.getString("ERROR_CD"));
				if( map == null ) {
					map = new HashMap<String,String>();
					errorMap.put(rs.getString("ERROR_CD"),map);
				}
				map.put(rs.getString("PRTNR_ERROR_CD"),rs.getString("PRTNR_ERROR_DESC"));
			}
		});				
	}

	private void processRespTemplateDetail() {
		
		// this template is optional so kill the processing if nothing specified.
		// this is a stub as we need it even if response is not generated

		props.put("responseWriter","responseWriter");
		if( respTemplateRowId == null)
			return;
		
		String sql = "SELECT * FROM CX_FINT_TMPLATR WHERE TMPL_ID = '"+respTemplateRowId+"' ";
		
		if(respFileFormat.equals("Delimited") )
			sql = sql + " ORDER BY TYPE, SEQ_NUM";
		else
			sql = sql + " ORDER BY TYPE, START_POSTN";
		
		log.debug(sql);
		
		// provide dummy maps that we can then throw away as we don't need mappings for response fields
		
		RespDetailTemplateHandler th = new RespDetailTemplateHandler(respFileFormat, props.getProperty("respDelimiter"),hfRespMappings, dtlRespMappings,
												props.getProperty("respHeaderPrefix"), props.getProperty("respDetailPrefix"), props.getProperty("respFooterPrefix"));
		
		getJdbcTemplate().query(sql,th);
		
		if(th.hdrFields.length()>0)
			th.hdrFields.deleteCharAt(th.hdrFields.length()-1);
		if(th.ftrFields.length()>0)
			th.ftrFields.deleteCharAt(th.ftrFields.length()-1);
		if(th.dtlFields.length()>0)
			th.dtlFields.deleteCharAt(th.dtlFields.length()-1);

		if(th.hdrSelect.length()>0)
			th.hdrSelect.deleteCharAt(th.hdrSelect.length()-1);
		if(th.ftrSelect.length()>0)
			th.ftrSelect.deleteCharAt(th.ftrSelect.length()-1);
		if(th.dtlSelect.length()>0)
			th.dtlSelect.deleteCharAt(th.dtlSelect.length()-1);
		
		// setup the properties accordingly
		
		props.put("respHeaderFields", th.hdrFields.toString());
		
		// we decide what kind of writer to use - one that has a NULL value in the header callback or not
		props.put("responseWriter", props.getProperty("respHeaderFields").length() >0?"responseWriter":"responseWriterWithNullHeader");
		
		props.put("respFooterFields", th.ftrFields.toString());
		props.put("respDetailFields", th.dtlFields.toString());
		
		// SELECT clauses
		props.put("respHeaderSelect", th.hdrSelect.toString());
		props.put("respFooterSelect", th.ftrSelect.toString());
		props.put("respDetailSelect", th.dtlSelect.toString());
		
		// if the format is delimited, then trim the delimiter from the end
		if( respFileFormat.equals("Delimited")) {
			// if delimited, then use the format, but strip the last delimiter
			if(th.hdrCols.length()>0)
				th.hdrCols.deleteCharAt(th.hdrCols.length()-1);
			if(th.ftrCols.length()>0)
				th.ftrCols.deleteCharAt(th.ftrCols.length()-1);
			if(th.dtlCols.length()>0)
				th.dtlCols.deleteCharAt(th.dtlCols.length()-1);
		}
		// either way, delimited or fixed, we are using a formatter output
		props.put("respHeaderFixedFormat", th.hdrCols.toString());
		props.put("respFooterFixedFormat", th.ftrCols.toString());
		props.put("respDetailFixedFormat", th.dtlCols.toString());
	}
	
	private void processRespTemplateHeader() {
		String sql = "SELECT * FROM CX_FINT_TMPL WHERE ROW_ID = "+
				"(SELECT OUT_TEMPLATE_ID FROM CX_FINT_INTG WHERE ROW_ID='"+intId+"')";
		
		log.debug(sql);
		
		respTemplateRowId = null;
		getJdbcTemplate().query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				
				respTemplateRowId = rs.getString("ROW_ID");
				respFileFormat = rs.getString("FILE_FORMAT");

				if( rs.getString("TRAILER_IDN") == null)
					props.put("respFooterPrefix", "__LM__NO__VALUE__");
				else
					props.put("respFooterPrefix", rs.getString("TRAILER_IDN"));
				
				if( rs.getString("HEADER_IDN") == null)
					props.put("respHeaderPrefix", "__LM__NO__VALUE__");
				else
					props.put("respHeaderPrefix", rs.getString("HEADER_IDN"));

				if( rs.getString("DETAIL_IDN") == null)
					props.put("respDetailPrefix", "__LM__NO__VALUE__");
				else
					props.put("respDetailPrefix", rs.getString("DETAIL_IDN"));
						
				if(respFileFormat.equals("Delimited")) {
					props.put("respDelimiter", rs.getString("FILE_DELIMITER"));
				} else {
					props.put("respDelimiter", ",");
				}
			}
		});

		// put some boiler plate in case there was no response template
		if(respTemplateRowId == null) {
			props.put("genResponse","no");
		}
		else
		{
			props.put("genResponse", "yes");
		}
	}

	/**
	 * Process the template fields one by one.
	 */
	private void processTemplateDetail() {
		String sql = "SELECT * FROM CX_FINT_TMPLATR WHERE TMPL_ID = '"+templateRowId+"' ";
		
		if(fileFormat.equals("Delimited") )
			sql = sql + " ORDER BY TYPE, SEQ_NUM";
		else
			sql = sql + " ORDER BY TYPE, START_POSTN";
		
		log.debug(sql);
		
		DetailTemplateHandler th = new DetailTemplateHandler(fileFormat, props.getProperty("delimiter"),hfMappings, dtlMappings);
		
		getJdbcTemplate().query(sql,th);
		
		if(th.hdrFields.length()>0)
			th.hdrFields.deleteCharAt(th.hdrFields.length()-1);
		if(th.ftrFields.length()>0)
			th.ftrFields.deleteCharAt(th.ftrFields.length()-1);
		if(th.dtlFields.length()>0)
			th.dtlFields.deleteCharAt(th.dtlFields.length()-1);
		
		if(th.hdrCols.length()>0)
			th.hdrCols.deleteCharAt(th.hdrCols.length()-1);
		if(th.ftrCols.length()>0)
			th.ftrCols.deleteCharAt(th.ftrCols.length()-1);
		if(th.dtlCols.length()>0)
			th.dtlCols.deleteCharAt(th.dtlCols.length()-1);
		
		// setup the properties accordingly
		
		props.put("hdrFields", th.hdrFields.toString());
		props.put("ftrFields", th.ftrFields.toString());
		props.put("dtlFields", th.dtlFields.toString());
		
		log.debug("dtlDBFields="+stripSkipped(th.dtlFields.toString()));
		props.put("dtlDBFields", stripSkipped(th.dtlFields.toString()));
		
		// if the format is fixed, then put the columns
		if( fileFormat.equals("Fixed")) {
			props.put("hdrFixedCols", th.hdrCols.toString());
			props.put("ftrFixedCols", th.ftrCols.toString());
			props.put("dtlFixedCols", th.dtlCols.toString());
		}
	}
	
	private String stripSkipped(String str) {
		StringBuffer dtlDBFields = new StringBuffer();
		for( String s : str.split(",") ) {
			if( !s.startsWith("__LM__SKIP"))
			dtlDBFields.append(s+",");
		}
		if(dtlDBFields.length() > 0)
			dtlDBFields.deleteCharAt(dtlDBFields.length()-1);

		return dtlDBFields.toString();
	}

	private void processIntegrationHeader() {
		//TODO: Remove all the specifics from here.
		String sql = "SELECT INT.*, GLPRDBU.NAME PROD_BU, GLPRGBU.NAME PROG_BU, P.NAME PARTNER_NAME, PRG.NAME PROG_NAME " +
                "FROM CX_FINT_INTG INT, S_ORG_EXT P, S_ORG_EXT PBU, S_ORG_EXT PRGBU, S_BU GLPRGBU, S_BU GLPRDBU, "+
                                    "S_LOY_PROGRAM PRG "+
                "WHERE INT.ROW_ID='"+intId+"' AND "+
                                 "P.ROW_ID = INT.PARTNER_ID AND "+
                                 "PRG.ROW_ID = INT.PROGRAM_ID AND "+
                                 "PBU.ROW_ID = P.BU_ID AND "+
                                 "PRGBU.ROW_ID = PRG.BU_ID AND "+
                                 "GLPRDBU.ROW_ID = P.BU_ID AND " +
                                 "GLPRGBU.ROW_ID = PRG.BU_ID";
		log.debug(sql);
		
		getJdbcTemplate().query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				try { 
				// sort out integration-level fields
					props.put("empOnwerId",rs.getString("ASSIGN_TO_ID"));
					props.put("minFileCount", Long.toString(rs.getLong("MIN_FILE_REQ")));
					props.put("integrationType", 
							getJdbcTemplate().queryForObject("SELECT NAME FROM S_LST_OF_VAL WHERE VAL='"+rs.getString("TYPE")+"'", String.class));
					
					if( rs.getString("FTP_ADDR") != null )
					{
						log.debug("Generating FTP INFO");
						props.put("ftpURL", rs.getString("FTP_ADDR"));
	
						if( rs.getString("FTP_AUTH_TYPE") != null && rs.getString("FTP_AUTH_TYPE").equals("File")) {
							log.debug("File authentication");
							props.put("ftpLogin", rs.getString("FTP_LOGIN"));
							props.put("ftpKeyFile", rs.getString("FTP_AUTH_FILE"));
							props.put("ftpAuthType", rs.getString("FTP_AUTH_TYPE"));
						} else if(rs.getString("FTP_AUTH_TYPE") != null && rs.getString("FTP_AUTH_TYPE").equals("Login")) {
							log.debug("Login authentication");
							props.put("ftpLogin", rs.getString("FTP_LOGIN"));
							props.put("ftpPassword", rs.getString("FTP_PASSWORD"));
							props.put("ftpAuthType", rs.getString("FTP_AUTH_TYPE"));
						} else
							throw new RuntimeException("You need to specify an FTP Authentication Type value of either File or Login.");
	
						props.put("ftpRemotePath",rs.getString("FTP_PATH"));
						String onDownloadAction = rs.getString("FTP_DOWNLOADED_TYPE");
						
						if( onDownloadAction  == null)
							throw new RuntimeException("Bad metadata: you need to specify an action upon SFTP download.");
						
						props.put("ftpDownloadAction", onDownloadAction);
						
						if( onDownloadAction.equals("Rename")) {
							if( rs.getString("FTP_DOWNLOADED_RENAME") != null )
								props.put("ftpRenamSuffix", rs.getString("FTP_DOWNLOADED_RENAME"));
							else
								throw new RuntimeException("Bad metadata: you specified rename on download for SFTP, but there is no rename suffix.");
						}
						
						// upload response file
						String uploadResponseFlag = rs.getString("UPLOAD_RESP_FLG");
						if( uploadResponseFlag != null && uploadResponseFlag.equals("Y")) {
							if( rs.getString("UPLOAD_PATH") != null )
								props.put("ftpRemoteRespPath", rs.getString("UPLOAD_PATH"));
						}
					}
				
					// we take the flag and based on that decide whether UPLOAD_PATH is to be used or not.
					if( rs.getString("UPLOAD_RESP_FLG") != null && rs.getString("UPLOAD_RESP_FLG").equals("Y") ) {
						props.put("ftpRespRemotePath", rs.getString("UPLOAD_PATH"));
					}
				}catch(Exception e) {
					log.debug(e.getStackTrace());
					throw new RuntimeException(e);
				}
			}
			
		});
	}
	
	protected void processEntitySpecifics() {
		String sql = "SELECT INT.*, PT.OBJECT_CD, PT.ATTR_TYPE_CD, PT.INTERNAL_NAME, PBU.NAME PROD_BU, PRGBU.NAME PROG_BU, P.NAME PARTNER_NAME, PRG.NAME PROG_NAME " + 
                "FROM CX_FINT_INTG INT, S_ORG_EXT P, S_ORG_EXT PBU, S_ORG_EXT PRGBU, "+
			             "S_LOY_PROGRAM PRG, S_LOY_ATTRDEFN PT "+
                "WHERE INT.ROW_ID='"+intId+"' AND "+
                		 "P.ROW_ID = INT.PARTNER_ID AND "+
                		 "PRG.ROW_ID = INT.PROGRAM_ID AND "+
                		 "PT.ROW_ID = INT.POINT_TYPE_ID AND "+
                		 "PBU.ROW_ID = P.BU_ID AND "+
                		 "PRGBU.ROW_ID = PRG.BU_ID";
		log.debug(sql);
		
		getJdbcTemplate().query(sql, new RowCallbackHandler() {
	
			public void processRow(ResultSet rs) throws SQLException {
				
				// figure out the program, partner and point_id issues		
				
				String prgBU = rs.getString("PROG_BU");
				String pBU = rs.getString("PROD_BU");
				String partnerName = rs.getString("PARTNER_NAME");
				String progName = rs.getString("PROG_NAME");
				
				props.put("partnerName", partnerName);
				props.put("programName", progName);
				
				// alter some of the default fields to provide org names mostly and the point type details
				// we don't want to rely on fixed columns.
				
				dtlMappings.get("TXN_BU").put("Default",prgBU);
				dtlMappings.get("VIS_BU").put("Default",prgBU);
				dtlMappings.get("PROG_BU").put("Default",prgBU);
				dtlMappings.get("PROD_BU").put("Default",prgBU);
				dtlMappings.get("MEMBER_MEM_BU").put("Default",prgBU);
				dtlMappings.get("MEMBER_PROG_BU").put("Default",prgBU);
				dtlMappings.get("POINT_PROG_BU").put("Default",prgBU);
				
				dtlMappings.get("PARTNER_ACCNT_BU").put("Default",pBU);
				dtlMappings.get("PROD_VEN_BU").put("Default",pBU);	
				
				dtlMappings.get("POINT_ATTR_TYPE_CD").put("Default", rs.getString("ATTR_TYPE_CD"));
				dtlMappings.get("POINT_INTERNALNAME").put("Default", rs.getString("INTERNAL_NAME"));
				dtlMappings.get("POINT_OBJECT_CD").put("Default", rs.getString("OBJECT_CD"));
			}
		});
	}
	
	protected String getEntitySpecificContextFile() {
		return (tableMap.get(props.get("prestageTable"))).get("ContextFile").toString();
	}

	/**
	 * Write the parsing components.
	 */
	private void processTemplateHeader() {
		// process the template - should be one record.
		
		String sql = "SELECT * FROM CX_FINT_TMPL WHERE ROW_ID = "+
				"(SELECT IN_TEMPLATE_ID FROM CX_FINT_INTG WHERE ROW_ID='"+intId+"')";
		
		log.debug(sql);
		
		getJdbcTemplate().query(sql, new RowCallbackHandler() {
										public void processRow(ResultSet rs) throws SQLException {
											
											Map<String, Object> stg = tableMap.get(rs.getString("STG_TBL_NAME"));
											
											templateRowId = rs.getString("ROW_ID");
											fileFormat = rs.getString("FILE_FORMAT");
											
											if( fileFormat.equals("Delimited"))
													props.put("delimiter",rs.getString("FILE_DELIMITER"));
											else
													// dummy has to be there for this to work even in fixed configuration.
													// TODO: consider composing only the right pieces
													props.put("delimiter", ",");
											
											props.put("prestageTable", rs.getString("STG_TBL_NAME"));
											props.put("eimTable", stg.get("EIMTable"));
											props.put("targetTable", stg.get("BaseTable"));
											props.put("IFBFile", props.get("eimTable")+".ifb");
											
											
											
											if(fileFormat.equals("Delimited")) {
												props.put("hdrTokenizer","headerDelimitedTokenizer");
												props.put("ftrTokenizer", "footerDelimitedTokenizer");
												props.put("dtlTokenizer", "detailDelimitedTokenizer");
												
												// stub these out so we don't have complaints
												props.put("hdrFixedCols", "1-1");
												props.put("ftrFixedCols", "1-1");
												props.put("dtlFixedCols", "1-1");
											}
											else {
												props.put("hdrTokenizer","headerFixedTokenizer");
												props.put("ftrTokenizer", "footerFixedTokenizer");
												props.put("dtlTokenizer", "detailFixedTokenizer");												
											}
											
											String hdrInd = rs.getString("HEADER_IDN");
											
											// handle the tricky part with the prefixes
											if( hdrInd == null || 
												hdrInd.trim().equals("") || hdrInd.trim().toLowerCase().equals("none")) {
												// configure it to not skip, and put in a prefix that will never match
												props.put("skipLines","0");
												props.put("hdrPattern", "__LM__NO__VALUE__");
											}
											else if(hdrInd.trim().toLowerCase().equals("firstline()")) {
												// configure it to skip (and handle) the first line
												props.put("skipLines","1");
												props.put("hdrPattern","__LM__NO__VALUE__");
											} else {
												// we are left with some kind of prefix
												props.put("skipLines","0");
												props.put("hdrPattern", hdrInd+"*");
											}
											
											String ftrInd = rs.getString("TRAILER_IDN");
											
											if( ftrInd == null || ftrInd.trim().equals("") || ftrInd.trim().toLowerCase().equals("none") ) {
												props.put("ftrPattern","__LM__NO__VALUE__");
											} else {
												// there is a footer prefix
												props.put("ftrPattern", ftrInd + "*");
											}
											
											String dtlInd = rs.getString("DETAIL_IDN");
											if( dtlInd == null || dtlInd.trim().equals("") || dtlInd.trim().equals("none")) {
												props.put("dtlPattern","*");
											} else {
												// there is a detail prefix
												props.put("dtlPattern",dtlInd + "*");
											}
										}
								});
	}
	
	public String getDestFolder() {
		return destFolder;
	}
	public void setDestFolder(String destFolder) {
		this.destFolder = destFolder;
	}

	public Map<String, Map<String, Object>> getTableMap() {
		return tableMap;
	}

	public void setTableMap(Map<String, Map<String, Object>> eimToBaseMapping) {
		this.tableMap = eimToBaseMapping;
	}

	public String getIntId() {
		return intId;
	}

	public HashMap<String, Map<String, String>> getDtlMappings() {
		return dtlMappings;
	}

	public HashMap<String, Map<String, String>> getHfMappings() {
		return hfMappings;
	}

	public HashMap<String, List<String>> getEventMap() {
		return eventMap;
	}

	public HashMap<String, Map<String, String>> getErrorMap() {
		return errorMap;
	}

	public HashMap<String, Map<String, String>> getAlertMap() {
		return alertMap;
	}

	public Properties getProps() {
		return props;
	}

	// this is just so that interface specifics can use the
	// same template without having to setup a data source, etc.
	// maybe not the best way to do this.
	
	protected JdbcTemplate borrowJdbcTemplate() {
		return getJdbcTemplate();
	}

	public EntitySpecifics getEntitySpecifics() {
		return entitySpecifics;
	}

	public void setEntitySpecifics(EntitySpecifics entitySpecifics) {
		this.entitySpecifics = entitySpecifics;
	}
}
