package com.loyaltymethods.fx.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * Decide whether to re-validate or not. Basically detect if we are in a 
 * re-run scenario and get the dbPrevalidateStep going.
 * 
 * @author Emil
 *
 */
public class RevalidationDecider implements JobExecutionDecider {
	Logger log = Logger.getLogger(RevalidationDecider.class);
	
	public FlowExecutionStatus decide(JobExecution jobExecution,
			StepExecution stepExecution) {
		
		log.debug("Entered the DECIDER");		
		FlowExecutionStatus exit = new FlowExecutionStatus("CONTINUE");
		
		if(jobExecution.getExecutionContext().get("Rerun") == null ) {
			// put it there for next time - this is the first execution
			stepExecution.getJobExecution().getExecutionContext().put("Rerun","true");
		}
		else {
			// this is a re-run, so if our current exit status is COMPLETE, then 
			// change it.
			exit = new FlowExecutionStatus("REVALIDATE");
		}
		log.debug("Decided to " + exit.toString());
		return exit;
	}

}
