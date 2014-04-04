package com.loyaltymethods.fx.step;

import org.apache.log4j.Logger;

import com.loyaltymethods.fx.data.IntegrationFileDAO;
import com.loyaltymethods.fx.enums.IntegrationRecordStatus;


/**
 * Step that will start Workflow for a given FILE_ID.
 * It then captures the task id and monitors until the 
 * task is completed.
 * 
 * Finally it runs a query to see how many records are 
 * imported, and reflects that in the CX_FINT_TXN.
 * 
 * @author Ravi
 *
 */
public class WFTasklet extends SrvrMgrTasklet {
	Logger log = Logger.getLogger(WFTasklet.class);
	private String component;
	private String processName;
	private String searchSpec;
	private String command;
	private String rerun;
	private IntegrationFileDAO intFileDAO;
	
	@Override
	protected String buildCommand() {
		/*
		 * Check to see if this is re-run. [Read the 'isRerun' flag from jobExecutionContext]
		 * For rerun; replace the "Record Status" under searchSpec command parameter from 'Prestaged' to 'Queued'.
		 */
		/*
		log.debug("WFTasklet>>isRerun = " + isRerun());
		
		if(isRerun() != null && isRerun().equalsIgnoreCase("true")) {
			log.debug("Rerun of Workflow...");
			// Rerun: alter the command
			if(getCommand() != null) {
				log.debug("WFTasklet>>getCommand() before alter = " + getCommand());
				setCommand(getCommand().replace(IntegrationRecordStatus.PRESTAGED.getStatus(), IntegrationRecordStatus.QUEUED.getStatus()));
				log.debug("WFTasklet>>getCommand() after alter = " + getCommand());
			}
			log.debug("WFTasklet>>getCommand() before alter = " + getSearchSpec());
			setSearchSpec(getSearchSpec().replace(IntegrationRecordStatus.PRESTAGED.getStatus(), IntegrationRecordStatus.QUEUED.getStatus()));
			log.debug("WFTasklet>>getCommand() after alter = " + getSearchSpec());
		}
		*/
		if(getCommand() != null) {
			return getCommand();
		}
		return "start task for comp "+getComponent()+" with ProcessName='"+getProcessName()+"',SearchSpec='"+getSearchSpec()+"'";
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getSearchSpec() {
		return searchSpec;
	}

	public void setSearchSpec(String searchSpec) {
		this.searchSpec = searchSpec;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String isRerun() {
		return rerun;
	}

	public void setRerun(String rerun) {
		this.rerun = rerun;
	}

	public IntegrationFileDAO getIntFileDAO() {
		return intFileDAO;
	}

	public void setIntFileDAO(IntegrationFileDAO intFileDAO) {
		this.intFileDAO = intFileDAO;
	}

	@Override
	public String toString() {
		return "WFTasklet [component=" + component + ", processName="
				+ processName + ", searchSpec="
				+ searchSpec + ", command=" + command + ", rerun=" + rerun + ", toString()="
				+ super.toString() + "]";
	/*
	 		return super.toString()
				+ String.format(", startTime=%s, endTime=%s, lastUpdated=%s, status=%s, exitStatus=%s, job=[%s]",
						startTime, endTime, lastUpdated, status, exitStatus, jobInstance);
	 */
	}
	
}
