package com.loyaltymethods.fx.step;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.loyaltymethods.fx.data.IntegrationFileDAO;

/**
 * An intelligent EIM geared for transactions specifically.
 * 
 * Specifically we deal with 2 situations:
 * 
 * 1) When the IFB filter has tobe specified to srvrmgr command line
 * 2) When we need to check if anything is staged.
 * 
 * @author Emil
 *
 */
public class EIMTasklet extends SrvrMgrTasklet {
	Logger log = Logger.getLogger(EIMTasklet.class);
	private String IFBFile;
	private DataSource sblDS;
	private String eimTable;
	private IntegrationFileDAO intFileDAO;
	private String txnTypeFilter;
	private String fileId;
	
	@Override
	protected String buildCommand() {
		return "start task for component EIM with Config='"+IFBFile+
									"', ExtendedParams=\\\"BatchRange="+getEIMRange()+" "+genFilterClause()+"\\\"";
	}
	
	
	protected String getEIMRange() {
		JdbcTemplate dbt = new JdbcTemplate(sblDS);
		return (String)dbt.queryForObject("SELECT MIN(IF_ROW_BATCH_NUM) || '-' || MAX(IF_ROW_BATCH_NUM)"+
										  "  FROM SIEBEL."+eimTable+" "+
										  " WHERE FILE_ID ='"+getFileId()+"' AND IF_ROW_BATCH_NUM > 0", 
							String.class);
	}
	
	/*
	 * Supplies filtering based on whether we are running Accruals, Redemptions or Both.
	 * 
	 * @see com.loyaltymethods.fx.step.RunEIMStep#genFilterClause()
	 */
	protected String genFilterClause() {
		if(txnTypeFilter == null) {
			log.debug("EIM Filter: "+",f1='Accrual',f2='Redemption'");
			return ",f1='Accrual',f2='Redemption'";
		}
		else {
			log.debug("EIM Filter: "+",f1="+getTxnTypeFilter()+",f2="+getTxnTypeFilter());
			return ",f1="+getTxnTypeFilter()+",f2="+getTxnTypeFilter();
		}
	}
	
	@Override
	protected boolean hasStagedRecords() {
		String filter = txnTypeFilter;
		log.debug("Initial filter setting: "+filter);
		
		if(filter == null) {
			filter = " TYPE_CD IN ('Accrual','Redemption') ";
		} else {
			filter = " TYPE_CD IN ("+txnTypeFilter+")";
		}
		
		log.debug("Final filter setting: "+filter);
		return ( getIntFileDAO().getStagedCount(filter) > 0);
	}
	
	public Logger getLog() {
		return log;
	}


	public void setLog(Logger log) {
		this.log = log;
	}


	public String getIFBFile() {
		return IFBFile;
	}


	public void setIFBFile(String iFBFile) {
		IFBFile = iFBFile;
	}


	public DataSource getSblDS() {
		return sblDS;
	}


	public void setSblDS(DataSource sblDS) {
		this.sblDS = sblDS;
	}


	public String getEimTable() {
		return eimTable;
	}


	public void setEimTable(String eimTable) {
		this.eimTable = eimTable;
	}


	public IntegrationFileDAO getIntFileDAO() {
		return intFileDAO;
	}


	public void setIntFileDAO(IntegrationFileDAO intFileDAO) {
		this.intFileDAO = intFileDAO;
	}


	public String getTxnTypeFilter() {
		return txnTypeFilter;
	}

	public void setTxnTypeFilter(String txnTypeFilter) {
		this.txnTypeFilter = txnTypeFilter;
	}
	
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}	
}
