package com.loyaltymethods.fx.step;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Required;

import com.loyaltymethods.fx.data.IntegrationFileDAO;

/**
 * Operation sequence decider is used to decide whether to separate Accruals/Redemptions
 * into two separate sequential jobs, or just run the file.
 * 
 * It does this by querying the TYPE_CD in CX_FINT_TXN to see what types of records are
 * available.
 * 
 *  * @author Emil
 *
 */
public class TxnOpSequenceDecider implements JobExecutionDecider {
	private IntegrationFileDAO intFile;
	
	public FlowExecutionStatus decide(JobExecution jobExecution,
			StepExecution stepExecution) {
		
		if(intFile.hasAccrualAndRedemption())
			return new FlowExecutionStatus("SEQUENCE");
		else
			return new FlowExecutionStatus("NOSEQUENCE");
	}

	public IntegrationFileDAO getIntFile() {
		return intFile;
	}
	
	@Required
	public void setIntFile(IntegrationFileDAO intFile) {
		this.intFile = intFile;
	}
}
