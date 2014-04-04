package com.loyaltymethods.fx.meta;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;

public class MemberSpecifics implements EntitySpecifics {
	Logger log = Logger.getLogger(MemberSpecifics.class);
	
	public void processEntitySpecifics(final MetaGenerator metaGen) {
		
		// set this stupidity to configure Member Check SQL 
		metaGen.getProps().put("memberStatusList","'Active','Inactive'");
		metaGen.getProps().put("memberNumberColumn", "MEM_NUM");
		metaGen.getProps().put("piggyBackUpdate", "");
		
		// configure post-EIM row-id lingage.
		// set row id column in EIM table
		metaGen.getProps().put("eimRowIdColumn", "T_LOYMEMBER__RID");
		// set connecting column
		metaGen.getProps().put("eimConnectingColumn", "MEM_NUM");		
				
		String sql = "SELECT PRGBU.NAME PROG_BU, P.NAME PARTNER_NAME, PRG.NAME PROG_NAME " + 
                "FROM SIEBEL.CX_FINT_INTG INT, SIEBEL.S_ORG_EXT P, S_ORG_EXT PBU, S_ORG_EXT PRGBU, "+
			             "SIEBEL.S_LOY_PROGRAM PRG "+
                "WHERE INT.ROW_ID='"+metaGen.getIntId()+"' AND "+
                		 "P.ROW_ID = INT.PARTNER_ID AND "+
                		 "PRG.ROW_ID = INT.PROGRAM_ID AND "+
                		 "PBU.ROW_ID = P.BU_ID AND "+
                		 "PRGBU.ROW_ID = PRG.BU_ID";
		log.debug(sql);
		
		metaGen.borrowJdbcTemplate().query(sql, new RowCallbackHandler() {
	
			public void processRow(ResultSet rs) throws SQLException {
				
				// figure out the program, partner and point_id issues		
				
				String prgBU = rs.getString("PROG_BU");
				String partnerName = rs.getString("PARTNER_NAME");
				String progName = rs.getString("PROG_NAME");
				
				metaGen.getProps().put("partnerName", partnerName);
				metaGen.getProps().put("programName", progName);
				
				// alter some of the default fields to provide org names mostly and the point type details
				// we don't want to rely on fixed columns.
				
				metaGen.getDtlMappings().get("PROG_BU").put("Default",prgBU);
				metaGen.getDtlMappings().get("MEM_BU").put("Default",prgBU);
				metaGen.getDtlMappings().get("PROG_NAME").put("Default",progName);
			}
		});
	}
}
