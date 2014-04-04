package com.loyaltymethods.fx.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.loyaltymethods.fx.data.IntegrationFileDAO;
import com.loyaltymethods.fx.ex.FXException;

/**
 * LoyEngineWaitStep is a simple step that simply waits for transactions
 * to no longer be queued for a specific FILE_ID. This step can timeout
 * based on the timeout parameter, which specifies how long we can wait
 * without any change in the "Queued" status of records.
 * 
 * An error will be thrown if we can not finish processing as this usually
 * requires manual intervention.
 * 
 * @author Emil
 *
 */
public class LoyEngineWaitStep implements Tasklet {
	Logger log = Logger.getLogger(LoyEngineWaitStep.class);
	
	private long timeout;							// number of milliseconds to timeout on
	private long interval;  						// frequency in milliseconds with which to check for Queued transactions
	
	private IntegrationFileDAO intFileDAO; 			// integration file data object
	
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {

		long inactivityTimer = timeout;
		
		long freshCount = intFileDAO.getQueuedCount();
		long prevCount = freshCount;
		
		while( inactivityTimer > 0 && freshCount > 0 && freshCount == prevCount)	{
			
			Thread.sleep(interval);
			inactivityTimer-=interval;
			
			log.debug("FreshCount: " + freshCount + ", prevCount: "+prevCount + ", inactivityTimer: "+inactivityTimer);

			freshCount = intFileDAO.getQueuedCount();
		}
		
		// check if by some miracle we are done with our file
		if( intFileDAO.getFileQueuedCount() == 0) {
			log.debug("FileCount == 0 --> Done waiting for loyalty engine.");
			return RepeatStatus.FINISHED;
		}
		
		if(inactivityTimer <= 0)
			throw new FXException("Loyalty engine timeout detected. Please check that the engine is running and re-start the current file.");
		else {
			log.debug("Continuing to wait for Engine");
			return RepeatStatus.CONTINUABLE;
		}
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public IntegrationFileDAO getIntFileDAO() {
		return intFileDAO;
	}

	public void setIntFileDAO(IntegrationFileDAO intFileDAO) {
		this.intFileDAO = intFileDAO;
	}

}
