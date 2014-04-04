package com.loyaltymethods.fx.file;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Required;

import com.loyaltymethods.fx.data.ErrorDAO;
import com.loyaltymethods.fx.data.IntegrationFileDAO;
import com.loyaltymethods.fx.ex.FXException;
import com.loyaltymethods.fx.log.util.FXLogger;
import com.loyaltymethods.fx.run.AlertManager;
import com.loyaltymethods.fx.run.EventManager;
import com.loyaltymethods.fx.run.EventManager.EventExit;

/**
 * The job of this class is to initialize the CX_FINT_FILE record
 * before the job starts.
 * 
 * @author Emil
 *
 */
public class FileJobListener implements JobExecutionListener {
	Logger log = Logger.getLogger(FileJobListener.class);
	
	IntegrationFileDAO intFileDAO;

	private AlertManager alerts;
	private EventManager events;
	private ErrorDAO errors;

	private long totalErrorThreshold;
	
	private EventExit fireEvent(String type) {
		EventExit result = events.fireEvent(type);
		if( result == EventExit.ERROR_STOP ) {
			throw new RuntimeException( new FXException("Event '"+type+"' aborted the file. Check the logs."));
		}
		return result;
	}
	
	public void beforeJob(JobExecution jobExecution) {
		String fileId = jobExecution.getJobInstance().getJobParameters().getString("fileId");
		
		jobExecution.getExecutionContext().put("fileId", fileId);
		jobExecution.getExecutionContext().put("fileName", intFileDAO.lookupFileName(fileId));
		
		boolean rerun = false;
		if(jobExecution.getExecutionContext().get("isRerun") == null) {
			// This is first run ...
			jobExecution.getExecutionContext().put("isRerun", "false");
			rerun = false;
		} else {
			// This is re-run...
			jobExecution.getExecutionContext().put("isRerun", "true");
			rerun = true;
		}
		
		String fileName = (String)jobExecution.getExecutionContext().get("fileName");
		
		if(rerun) {
			fileName = "rerun_" + fileName + "_" + jobExecution.getId();
		}
		FXLogger.setLoggingDiagnosticMessage(fileName);

		// set it explicitly here
		
		intFileDAO.setFileId(fileId);
		intFileDAO.updateBatchJobFK(fileId, jobExecution.getJobInstance().getId());
		
		intFileDAO.updateLogFile(fileName);
		
		// give the file id to the event manager

		events.addSubst("${ctx.fileId}", fileId);
		events.addSubst("${ctx.fileName}", jobExecution.getExecutionContext().getString("fileName"));

		if( fireEvent("Before File") == EventExit.ERROR_CONTINUE )
			jobExecution.setExitStatus(ExitStatus.FAILED);
	}

	public void afterJob(JobExecution jobExecution) {
		String fileName = jobExecution.getExecutionContext().getString("fileName");
		
		log.debug("afterJob: ExitStatus="+jobExecution.getExitStatus());
		log.debug("afterJob: Status="+jobExecution.getStatus());
		
		log.debug("afterJob: StepEvent = '" + jobExecution.getExecutionContext().get("StepEvent") +
				  ", AllFailureExceptions.size()="+jobExecution.getAllFailureExceptions().size());
		
		long errorCount = intFileDAO.getErrorCount();
		log.debug("Error count in file="+errorCount + ", threshold="+totalErrorThreshold);

		if(errorCount > 0 
				|| jobExecution.getExecutionContext().get("StepEvent") != null
				|| jobExecution.getAllFailureExceptions().size()>0 ) {

			// even if no exceptions happened, we check the errors and fail the job
			// so it can be restarted when these errors are re-submitted.
			
			if( errorCount > totalErrorThreshold ) {
				log.debug("Exceeded total threshold, so marking job FAILED.");
				jobExecution.setExitStatus(ExitStatus.FAILED);
				jobExecution.setStatus(BatchStatus.FAILED);
			}
			else {
				log.debug("Threshold not met. Job status remains unchanged. Updating all Errors to Unfixable.");
				intFileDAO.changeRecordStatus(new String [] {"Error"},"Unfixable");
			}
		}
		
		// update when we finished, if we have at least some records
		// loaded then we are somewhat partially complete.

		Properties ps = new Properties();
		ps.put("STAGE","Finished");
		
		if( jobExecution.getStatus().equals(BatchStatus.FAILED) )
		{
			log.debug("Determined job failed - checking if anything got processed.");
			if( intFileDAO.getProcessedCount() > 0)
			{
				log.debug("getProcessedCount="+ intFileDAO.getProcessedCount());
				
				ps.put("ERROR_CODE","SBL-FINT-0012");
				ps.put("ERROR_DESC",errors.msg("SBL-FINT-0012"));
				
				log.debug("jobExecution.getAllFailureExceptions().size()=" + jobExecution.getAllFailureExceptions().size());
				if( jobExecution.getAllFailureExceptions().size()>0) {
					log.debug("Grabbing the last message: " + jobExecution.getAllFailureExceptions().get(0).getMessage());
					ps.put("ERROR_DESC", ps.get("ERROR_DESC")+" "+jobExecution.getAllFailureExceptions().get(0).getMessage());
				}

				ps.put("STATUS", "Partially Complete");
				
				alerts.error("ERROR: File '" + fileName +"'", 
								ps.getProperty("ERROR_DESC") + "\n Number of Errors found: "+intFileDAO.getErrorCount());
				
				log.debug("Job for file '"+fileName+"' compelted with PARTIAL FAILURE.");
			}
			else {
				ps.put("STATUS","Error");
				ps.put("ERROR_CODE","SBL-FINT-0013");
				ps.put("ERROR_DESC",errors.msg("SBL-FINT-0013"));
			
				if( jobExecution.getAllFailureExceptions().size()>0) 
					ps.put("ERROR_DESC", ps.get("ERROR_DESC")+" "+jobExecution.getAllFailureExceptions().get(0).getMessage());
				
				String stepEventIssue = (String)jobExecution.getExecutionContext().get("StepEvent");
				if( stepEventIssue != null )
					ps.put("ERROR_DESC", ps.get("ERROR_DESC")+"; step event failure: " + stepEventIssue);

				alerts.error("ERROR: File '" + fileName +"'", 
						ps.getProperty("ERROR_DESC"));
				
				log.debug("Job for file '"+fileName+"'compelted with FAILURE.");
			}
		}
		else {
			ps.put("STATUS", "Complete");
			alerts.success("SUCCESS: File '" + fileName + "'", 
							"The file was processed successfully. Number of records processed: "+ intFileDAO.getProcessedCount()+".");
			
			log.debug("Job for file '" + fileName +"' compelted with SUCCESS.");
		}
		
		events.addSubst("${ctx.fileStatus}", ps.getProperty("STATUS"));
		events.fireEvent("After File");

		intFileDAO.updateFile(ps);
		intFileDAO.updateEndDate();
		FXLogger.removeLoggingDiagnosticMessage();
	}

	// getter/setter 
	public IntegrationFileDAO getIntFileDAO() {
		return intFileDAO;
	}

	public void setIntFileDAO(IntegrationFileDAO intFileDAO) {
		this.intFileDAO = intFileDAO;
	}

	public AlertManager getAlerts() {
		return alerts;
	}

	@Required
	public void setAlerts(AlertManager alerts) {
		this.alerts = alerts;
	}

	public EventManager getEvents() {
		return events;
	}

	@Required
	public void setEvents(EventManager events) {
		this.events = events;
	}

	public ErrorDAO getErrors() {
		return errors;
	}

	@Required
	public void setErrors(ErrorDAO errors) {
		this.errors = errors;
	}

	public long getTotalErrorThreshold() {
		return totalErrorThreshold;
	}

	@Required
	public void setTotalErrorThreshold(long totalErrorThreshold) {
		this.totalErrorThreshold = totalErrorThreshold;
	}
}
