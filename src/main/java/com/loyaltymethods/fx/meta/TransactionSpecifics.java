package com.loyaltymethods.fx.meta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;

public class TransactionSpecifics implements EntitySpecifics {
	Logger log = Logger.getLogger(TransactionSpecifics.class);
	
	public void processEntitySpecifics(final MetaGenerator metaGen) {
				
		// this will be appended to the member validation sql to piggy back on it for this update.
		metaGen.getProps().put("piggyBackUpdate", ",MEM_ID = V_ROW_ID(i)");
		metaGen.getProps().put("memberStatusList","'Active'");
		metaGen.getProps().put("memberNumberColumn", "MEMBER_MEM_NUM");

		// set row id
		metaGen.getProps().put("eimRowIdColumn", "T_LOY_TXN__RID");
		
		// set connecting column
		metaGen.getProps().put("eimConnectingColumn", "TXN_NUM");
		
		String sql = "SELECT INT.*, PT.OBJECT_CD, PT.ATTR_TYPE_CD, PT.INTERNAL_NAME, PBU.NAME PROD_BU, PRGBU.NAME PROG_BU, P.LOC PARTNER_SITE, P.NAME PARTNER_NAME, PRG.NAME PROG_NAME " + 
                "FROM CX_FINT_INTG INT, S_ORG_EXT P, S_ORG_EXT PBU, S_ORG_EXT PRGBU, "+
			             "S_LOY_PROGRAM PRG, S_LOY_ATTRDEFN PT "+
                "WHERE INT.ROW_ID='"+metaGen.getIntId()+"' AND "+
                		 "P.ROW_ID = INT.PARTNER_ID AND "+
                		 "PRG.ROW_ID = INT.PROGRAM_ID AND "+
                		 "PT.ROW_ID = INT.POINT_TYPE_ID AND "+
                		 "PBU.ROW_ID = P.BU_ID AND "+
                		 "PRGBU.ROW_ID = PRG.BU_ID";
		log.debug(sql);
		
		metaGen.borrowJdbcTemplate().query(sql, new RowCallbackHandler() {
	
			public void processRow(ResultSet rs) throws SQLException {
				
				// figure out the program, partner and point_id issues		
				
				String prgBU = rs.getString("PROG_BU");
				String pBU = rs.getString("PROD_BU");
				String partnerName = rs.getString("PARTNER_NAME");
				String progName = rs.getString("PROG_NAME");
				String partnerLOC = rs.getString("PARTNER_SITE");
				
				// this is possible
				if( partnerLOC == null ) 
					partnerLOC = "Expr: NULL";
				
				metaGen.getProps().put("partnerName", partnerName);
				metaGen.getProps().put("programName", progName);
				
				// alter some of the default fields to provide org names mostly and the point type details
				// we don't want to rely on fixed columns.
				
				metaGen.getDtlMappings().get("TXN_BU").put("Default",prgBU);
				metaGen.getDtlMappings().get("VIS_BU").put("Default",prgBU);
				metaGen.getDtlMappings().get("PROG_BU").put("Default",prgBU);
				metaGen.getDtlMappings().get("PROD_BU").put("Default",prgBU);
				metaGen.getDtlMappings().get("MEMBER_MEM_BU").put("Default",prgBU);
				metaGen.getDtlMappings().get("MEMBER_PROG_BU").put("Default",prgBU);
				metaGen.getDtlMappings().get("POINT_PROG_BU").put("Default",prgBU);
				
				metaGen.getDtlMappings().get("PARTNER_ACCNT_BU").put("Default",pBU);
				metaGen.getDtlMappings().get("PROD_VEN_BU").put("Default",pBU);
				metaGen.getDtlMappings().get("PROD_VEN_LOC").put("Default",partnerLOC);
				metaGen.getDtlMappings().get("PARTNER_ACCNT_LOC").put("Default",partnerLOC);
				
				metaGen.getDtlMappings().get("POINT_ATTR_TYPE_CD").put("Default", rs.getString("ATTR_TYPE_CD"));
				metaGen.getDtlMappings().get("POINT_INTERNALNAME").put("Default", rs.getString("INTERNAL_NAME"));
				metaGen.getDtlMappings().get("POINT_OBJECT_CD").put("Default", rs.getString("OBJECT_CD"));
			}
		});
		
		sql = "SELECT PRD.NAME PROD_NAME, VEN.NAME PROD_VEN_NAME, VEN.LOC PROD_VEN_LOC, ORG.NAME PROD_VEN_BU " +
				"FROM SIEBEL.CX_FINT_INTG INT, SIEBEL.S_PROD_INT PRD, SIEBEL.S_ORG_EXT VEN, SIEBEL.S_ORG_EXT ORG " +
				"WHERE INT.PRODUCT_ID = PRD.ROW_ID (+) AND PRD.VENDR_OU_ID = VEN.PAR_ROW_ID (+) AND VEN.BU_ID = ORG.ROW_ID (+) " +
				"AND INT.ROW_ID = '"+metaGen.getIntId()+"'";
		log.debug(sql);
		
		metaGen.borrowJdbcTemplate().query(sql, new RowCallbackHandler() {
	
			public void processRow(ResultSet rs) throws SQLException {
				String prodName = rs.getString("PROD_NAME");
				if(rs.wasNull()) prodName = "Expr: NULL";
				
				String prodVenName = rs.getString("PROD_VEN_NAME");
				if(rs.wasNull()) prodVenName = "Expr: NULL";
				
				String prodVenLoc = rs.getString("PROD_VEN_LOC");
				if(rs.wasNull()) prodVenLoc = "Expr: NULL";
				
				String prodVenBu = rs.getString("PROD_VEN_BU");
				if(rs.wasNull()) prodVenBu = "Expr: NULL";
				
				// Override default values for the following fields.
				log.debug("before:PROD_NAME>>" + metaGen.getDtlMappings().get("PROD_NAME"));
				if(metaGen.getDtlMappings().get("PROD_NAME") == null 
						|| (metaGen.getDtlMappings().get("PROD_NAME") != null && metaGen.getDtlMappings().get("PROD_NAME").get("Default") == null)) {
					// Add mapping when not provided in template.
					HashMap<String,String> prodNameMapping = new HashMap<String, String>();
					prodNameMapping.put("Default",prodName);
					prodNameMapping.put("EIM Mapping","EIM_LOY_TXN.PROD_NAME");
					prodNameMapping.put("Type","Character");
					prodNameMapping.put("Length","100");
					metaGen.getDtlMappings().put("PROD_NAME", prodNameMapping);
					log.debug("after:PROD_NAME>>" + metaGen.getDtlMappings().get("PROD_NAME"));

				}
				metaGen.getDtlMappings().get("PROD_VEN_NAME").put("Default",prodVenName);
				metaGen.getDtlMappings().get("PROD_VEN_LOC").put("Default",prodVenLoc);
				metaGen.getDtlMappings().get("PROD_VEN_BU").put("Default",prodVenBu);
			}
		});
	}
}
