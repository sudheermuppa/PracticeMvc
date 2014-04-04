package com.loyaltymethods.fx.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.loyaltymethods.fx.data.IntegrationFileDAO;

/**
 * This listener does the following after workflow step:
 * - Checks to see if job completed with no errors. 
 * - if no errors, it further checks to see if the records are processed successfully.
 * - if any records found with "Prestaged", the marks the step status as error.
 * 
 * @author Ravi
 *
 */
public class WorkflowStepListener implements StepExecutionListener {
	static Logger log = Logger.getLogger(WorkflowStepListener.class);
	private IntegrationFileDAO intFileDAO;
	
	/* (non-Javadoc)
	 * @see org.springframework.batch.core.StepExecutionListener#beforeStep(org.springframework.batch.core.StepExecution)
	 */
	public void beforeStep(StepExecution stepExecution) {
		// no handler for this event.

	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.StepExecutionListener#afterStep(org.springframework.batch.core.StepExecution)
	 */
	public ExitStatus afterStep(StepExecution stepExecution) {
		ExitStatus exit = stepExecution.getExitStatus();
		log.debug("WorkflowStepListener.afterStep()>>ExitStatus=" + exit);
		if(!hasStepExecutionFailed(stepExecution)) {
			// Check to see if all records are processed successfully.
			long totalRecordCount = intFileDAO.getTotalCount();
			long processedRecordCount = intFileDAO.getProcessedCount();
			log.debug("WorkflowStepListener.afterStep()>>totalRecordCount=" + totalRecordCount + ", processedRecordCount="+processedRecordCount);
			if(processedRecordCount != totalRecordCount) {
				// Set status as 'Failed'
				log.debug("WorkflowStepListener.afterStep()>>Setting step exit status to 'FAILED'");
				stepExecution.setExitStatus(ExitStatus.FAILED);
			}
		}
		return stepExecution.getExitStatus();
	}

	private boolean hasStepExecutionFailed(StepExecution stepExecution) {
		return ExitStatus.FAILED.getExitCode().equals(stepExecution.getExitStatus().getExitCode());
	}
	
	public IntegrationFileDAO getIntFileDAO() {
		return intFileDAO;
	}

	public void setIntFileDAO(IntegrationFileDAO intFileDAO) {
		this.intFileDAO = intFileDAO;
	}

}
