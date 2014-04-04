package com.loyaltymethods.fx.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Required;

import com.loyaltymethods.fx.data.IntegrationFileDAO;

/**
 * This one decides whether to generate a response or not. Basically this will
 * depend on whether there are any errors in the file, and if these errors are
 * ignored by the user or not.
 * 
 * @author Emil
 *
 */
public class ResponseDecider implements JobExecutionDecider {
	Logger log = Logger.getLogger(ResponseDecider.class);
	
	private IntegrationFileDAO intFile;
	private String genResponse;					// Yes/No flag for the response step
	private long totalErrorThreshold;			// error threshold - if we are below it, we can generate response
	
	public FlowExecutionStatus decide(JobExecution jobExecution,
			StepExecution stepExecution) {

		log.debug("Response Decider: intFile.getErrorCount()=" + 
					intFile.getErrorCount()+", totalErrorThreshold=" + totalErrorThreshold); 
				
		if( intFile.getErrorCount() > totalErrorThreshold || genResponse.trim().toLowerCase().equals("no")) {
			log.debug("No response generation required.");
			return new FlowExecutionStatus("NORESPONSE");
		}
		else {
			log.debug("Response will be generated.");
			return new FlowExecutionStatus("RESPONSE");
		}
	}

	public IntegrationFileDAO getIntFile() {
		return intFile;
	}

	@Required
	public void setIntFile(IntegrationFileDAO intFile) {
		this.intFile = intFile;
	}

	public String getGenResponse() {
		return genResponse;
	}

	@Required
	public void setGenResponse(String genResponse) {
		this.genResponse = genResponse;
	}

	public long getTotalErrorThreshold() {
		return totalErrorThreshold;
	}
	
	@Required
	public void setTotalErrorThreshold(long totalErrorThreshold) {
		this.totalErrorThreshold = totalErrorThreshold;
	}
}
