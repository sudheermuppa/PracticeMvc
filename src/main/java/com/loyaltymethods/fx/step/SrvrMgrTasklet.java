/**
 * 
 */
package com.loyaltymethods.fx.step;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.loyaltymethods.fx.ex.FXException;

/**
 * Executes the start task command returned by buildCommand() method using Siebel Server Manager command-line interface
 * Step that will start a given task for EIM or Workflow.
 * It then captures the task id and monitors until the 
 * task is completed.
 * 
 * Finally it runs a query to see how many records are 
 * imported, and reflects that in the CX_FINT_TXN.
 * 
 * 
 * @author Ravi
 *
 */
public abstract class SrvrMgrTasklet implements Tasklet {
	Logger log = Logger.getLogger(SrvrMgrTasklet.class);
	
	private String srvrmgrPath;
	private String siebelUser;
	private String siebelPwd;
	private String siebelGateway;
	private String siebelServer;
	private String siebelEnterprise;
	private long sleepInterval = 1000L;					// default check every 1s
	private long retryMaxCount = 18000L;				// default retry = timeout after 5hrs.
	
	/**
	 * Build and return a single command to be executed using Siebel Server Manager command-line interface
	 * the command must be bounded within double quotes
	 * i.e. start task for EIM or Workflow
	 * 
	 * @return 
	 */
	protected abstract String buildCommand();
	
	/**
	 * No filtering applied at the generic level.
	 * 
	 * @return
	 */
	protected boolean hasStagedRecords() {
		// return ( intFileDAO.getStagedCount(" 1=1 ") > 0);
		return true;
	}

	protected String startEIMTask() throws Exception {

		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		
		String commandString = buildCommand();
		pr = rt.exec(new String [] {srvrmgrPath,
									"/u",siebelUser,"/p",siebelPwd,"/e",siebelEnterprise,
									"/g",siebelGateway,"/s",siebelServer,
									"/c",
									"\""+commandString+"\""});

		log.debug("SRVRMGR cmdLine: "+
						"/u"+" "+siebelUser+" "+"/p"+" "+"****"+" "+"/e"+" "+siebelEnterprise+" "+
						"/g"+" "+siebelGateway+" "+"/s"+" "+siebelServer+" "+
						"/c"+" "+
						"\""+commandString+"\"");

		
		// now read in the standard output
		BufferedReader stdOut = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		
		String line;
		StringBuffer output = new StringBuffer();
		
		while((line = stdOut.readLine())!= null) {
			output.append(line+"\n");
			if(line.startsWith("SV_NAME")) {
				line = stdOut.readLine(); 			// skip through the ----- line
				output.append(line+"\n");
				if( line != null ) {
					line = stdOut.readLine();		// read the task line
					output.append(line+"\n");
					if( line != null ) {
						return line.split(" +")[2];
					}
				}
			}
		}
		log.error("Fatal: could not parse the srvrmgr output:"+output.toString());
		throw new FXException("Fatal: could not parse the srvrmgr output. See the error log for the srvrmgr output.");
	}
	
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		long retryCount = 0;
		
		if( !hasStagedRecords())
		{
			log.warn("Warning: attempt to run with 0 pre-staged records. EIM will not run.");
			return RepeatStatus.FINISHED;
		}
		
		// wait for the task to finish
		String taskId = startEIMTask();
		String status = getEIMTaskStatus(taskId);
		while( status.equals("Running")) {
			Thread.sleep(sleepInterval);
			if(retryCount++ == retryMaxCount) {
				throw new FXException("Fatal: srvrmgr has timed out on task id="+taskId+", last known status:"+getEIMTaskStatus(taskId));
			}
			status = getEIMTaskStatus(taskId);
		}
		
		if(status.equals("Exited")) {
			// EIM failed us 
			throw new FXException("EIM task with id="+taskId+" exited with fialure. Please see the EIM log for this task for more details.");
		}
		
		return RepeatStatus.FINISHED;
	}
	
	public String getEIMTaskStatus(String taskId) throws Exception {
		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		
		pr = rt.exec(new String [] {srvrmgrPath,"/u",siebelUser,"/p",siebelPwd,"/e",siebelEnterprise,"/g",siebelGateway,"/s",siebelServer,"/c",
				"\"list task "+taskId+"\""});
		
		// now read in the standard output
		BufferedReader stdOut = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		
		String line;
		StringBuffer output = new StringBuffer();
		
		while((line = stdOut.readLine())!= null) {
			output.append(line);
			if(line.startsWith("SV_NAME")) {
				line = stdOut.readLine(); 			// skip through the ----- line
				output.append(line);
				if( line != null ) {
					line = stdOut.readLine();		// read the task line
					output.append(line);
					if( line != null ) {
						log.debug("getEIMTaskStatus("+taskId+")>>" + line);
						return line.split(" +")[4];
					}
				}
			}
		}
		log.error("Fatal: could not parse the srvrmgr output:"+output.toString());
		throw new FXException("Fatal: could not parse the srvrmgr output. See the error log for the srvrmgr output."+line);
	}

	public String getSrvrmgrPath() {
		return srvrmgrPath;
	}

	public void setSrvrmgrPath(String srvrmgrPath) {
		this.srvrmgrPath = srvrmgrPath;
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	public String getSiebelUser() {
		return siebelUser;
	}

	public void setSiebelUser(String siebelUser) {
		this.siebelUser = siebelUser;
	}

	public String getSiebelPwd() {
		return siebelPwd;
	}

	public void setSiebelPwd(String siebelPwd) {
		this.siebelPwd = siebelPwd;
	}

	public String getSiebelGateway() {
		return siebelGateway;
	}

	public void setSiebelGateway(String siebelGateway) {
		this.siebelGateway = siebelGateway;
	}

	public String getSiebelServer() {
		return siebelServer;
	}

	public void setSiebelServer(String siebelServer) {
		this.siebelServer = siebelServer;
	}

	public String getSiebelEnterprise() {
		return siebelEnterprise;
	}

	public void setSiebelEnterprise(String siebelEnterprise) {
		this.siebelEnterprise = siebelEnterprise;
	}

	public long getSleepInterval() {
		return sleepInterval;
	}

	public void setSleepInterval(long sleepInterval) {
		this.sleepInterval = sleepInterval;
	}

	public long getRetryMaxCount() {
		return retryMaxCount;
	}

	public void setRetryMaxCount(long retryMaxCount) {
		this.retryMaxCount = retryMaxCount;
	}

	@Override
	public String toString() {
		return "SrvrMgrTasklet [srvrmgrPath=" + srvrmgrPath + ", siebelUser="
				+ "*****" + ", siebelPwd=" + "*****" + ", siebelGateway="
				+ siebelGateway + ", siebelServer=" + siebelServer
				+ ", siebelEnterprise=" + siebelEnterprise + ", sleepInterval="
				+ sleepInterval + ", retryMaxCount=" + retryMaxCount + "]";
	}	
	
}
