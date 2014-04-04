package com.loyaltymethods.fx.file;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Required;

import com.loyaltymethods.fx.data.IntegrationFileDAO;
import com.loyaltymethods.fx.ex.FXException;
import com.loyaltymethods.fx.run.EventManager;
import com.loyaltymethods.fx.run.EventManager.EventExit;

/**
 * The StepUpdater does the following:
 * 
 * - Sets stage and status for each step based before starting.
 * - Checks for errors at the end of each step and sets exit code and message accordingly.
 * - Translates step names to readable names
 * - Sends alerts
 * - Fires events
 * - Does demo sleep
 * 
 * @author Emil
 *
 */
public class FileStepListener implements StepExecutionListener {
	Logger log = Logger.getLogger(FileStepListener.class);
	
	// optional translation of step names to stages for CX_FINT_FILE 
	Map<String, String> nameToStage;
	IntegrationFileDAO intFileDAO;
	
	String demoSleep;
	String responseSuffix;				// kludge to allow for setting dynamic response file name 
	
	private EventManager events;
	
	private EventExit fireEvent(String type) {
		EventExit result = events.fireEvent(type);
		if( result == EventExit.ERROR_STOP ) {
			throw new RuntimeException( new FXException("Event '"+type+"' aborted the file. Check the logs."));
		}
		return result;
	}

	/**
	 * This translates a step from it's internal name to something more humanly readable.
	 * It does a contains() if it can't find a direct match.
	 * 
	 * @param stepName
	 * @return
	 */
	protected String getStage(String stepName) {
		String stage = stepName;
		
		if(nameToStage != null) {
			if( nameToStage.get(stage) != null)
				stage = nameToStage.get(stage);
			else {
				for(String name : nameToStage.keySet()) {
					if(stepName.contains(name))
						stage = nameToStage.get(name);
				}
			}
		}
		return stage;
	}

	public void beforeStep(StepExecution stepExecution) {
		log.debug("-------- Starting Step: "+stepExecution.getStepName()+" ----------------");
		intFileDAO.updateProcessingStatus(getStage(stepExecution.getStepName()), "Running","");
		//intFileDAO.updateStats();
		
		// we need to indicate in the job context if the event fired with ERROR_CONTINUE
		// so as to prevent the job from completing successfully. It's kludgy, but the only way to pass information to the job.

		events.addSubst("${ctx.stepName}", getStage(stepExecution.getStepName()));
		
		// if this is the response generator step, construct the right file name for it
		if( stepExecution.getStepName().contains("genResponse")) {
			
			String respFileName;
			
			log.debug("Response suffix is: "+responseSuffix);
			
			if( responseSuffix.trim().startsWith("Expr:")) {
				respFileName = intFileDAO.getExpr(responseSuffix.substring("Expr:".length()));
				log.debug("Used '"+responseSuffix.substring("Expr:".length())+"' to derive '"+respFileName+"'");
			}	
			else {
				respFileName = (String) stepExecution.getJobExecution().getExecutionContext().get("fileName");
				String ext = getFileExtension(respFileName);
				log.debug("File Extension of Current File: "+ext);
				log.debug("Current file: "+respFileName);
				if( ext.equals("")) {
					respFileName = respFileName + "." + responseSuffix;
					log.debug("Just added suffix: "+respFileName);
				}
				else {
					respFileName = respFileName.replaceAll("." + ext, "."+responseSuffix);
					log.debug("Replaced suffix: "+respFileName);
				}
			}
			log.debug("Putting this thing on the job context: "+respFileName);
			stepExecution.getJobExecution().getExecutionContext().put("respFileName",respFileName);
			events.addSubst("${ctx.responseFileName}", respFileName);
			Properties p = new Properties();
			p.put("RESP_FILE",respFileName);
			intFileDAO.updateFile(p);
		}

		// file is determined
		if(fireEvent("Before "+getStage(stepExecution.getStepName())) == EventExit.ERROR_CONTINUE)
			stepExecution.getJobExecution().getExecutionContext().put("StepEvent", stepExecution.getStepName()+"->Before() failed. Check the logs.");	
		
	}
	
	// determine file extension of a filename
	private String getFileExtension(String f) {
		   String ext = "";
		   int i = f.lastIndexOf('.');
		   if (i > 0 &&  i < f.length() - 1) {
		      ext = f.substring(i + 1);
		   }
		   return ext;
	}

	public ExitStatus afterStep(StepExecution stepExecution) {
		
		ExitStatus exit = stepExecution.getExitStatus();

		// we need to indicate in the job context if the event fired with ERROR_CONTINUE
		// so as to prevent the job from completing successfully. It's kludgy, but the only way to pass information to the job.
		
		if( exit == ExitStatus.FAILED )
			events.addSubst("${ctx.stepStatus}", "Error");
		else
			events.addSubst("${ctx.stepStatus}", "Complete");
		
		events.addSubst("${ctx.stepName}", getStage(stepExecution.getStepName()));
		
		if( fireEvent("After " + getStage(stepExecution.getStepName())) == EventExit.ERROR_CONTINUE )
			stepExecution.getJobExecution().getExecutionContext().put("StepEvent", stepExecution.getStepName()+"->After() failed. Check the logs.");
		
		if( demoSleep != null ) {
			try {
				Thread.sleep(Long.parseLong(demoSleep));
			} catch (NumberFormatException e) {
				log.warn(e.toString());
			} catch (InterruptedException e) {
				log.warn(e.toString());
			}
		}

		log.debug("Exit status: " + exit);
		
		String message = "";
		
		if( stepExecution.getFailureExceptions().size() > 0) {
			message = stepExecution.getFailureExceptions().get(0).getMessage();
			message = message.substring(0, Math.min(message.length(),249));
		}

		if( exit.getExitCode().equals("COMPLETED") || exit.getExitCode().equals("NOOP") || exit.getExitCode().equals("UNKNOWN") )
			intFileDAO.updateProcessingStatus(getStage(stepExecution.getStepName()), "Complete", message);
		else if( exit.getExitCode().equals("FAILED") )
			intFileDAO.updateProcessingStatus(getStage(stepExecution.getStepName()), "Error", message);
		
		intFileDAO.updateStats();
		
		log.debug("-------- Finishing Step: "+stepExecution.getStepName()+" ----------------");
		return exit;
	}
	
	public Map<String, String> getNameToStage() {
		return nameToStage;
	}
	
	public void setNameToStage(Map<String, String> nameToStage) {
		this.nameToStage = nameToStage;
	}

	public IntegrationFileDAO getIntFileDAO() {
		return intFileDAO;
	}

	public void setIntFileDAO(IntegrationFileDAO intFileDAO) {
		this.intFileDAO = intFileDAO;
	}

	public String getDemoSleep() {
		return demoSleep;
	}

	public void setDemoSleep(String demoSleep) {
		this.demoSleep = demoSleep;
	}

	public EventManager getEvents() {
		return events;
	}

	public void setEvents(EventManager events) {
		this.events = events;
	}

	public String getResponseSuffix() {
		return responseSuffix;
	}
	
	@Required
	public void setResponseSuffix(String responseSuffix) {
		this.responseSuffix = responseSuffix;
	}
}