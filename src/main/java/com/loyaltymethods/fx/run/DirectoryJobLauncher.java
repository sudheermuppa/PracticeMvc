package com.loyaltymethods.fx.run;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.Properties;


import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Required;

import com.loyaltymethods.fx.data.ErrorDAO;
import com.loyaltymethods.fx.data.IntegrationDAO;
import com.loyaltymethods.fx.data.IntegrationFileDAO;
import com.loyaltymethods.fx.ex.FXException;
import com.loyaltymethods.fx.ex.FXNotEnoughFilesException;
import com.loyaltymethods.fx.run.EventManager.EventExit;

/**
 * The class is responsible for:
 * 
 * - Downloading files
 * - Checking for duplicate files
 * - Checking for empty files
 * - Processing each individual file in a job
 * 
 * @author Emil
 *
 */
public class DirectoryJobLauncher {
	Logger log = Logger.getLogger(DirectoryJobLauncher.class);
	
	private SFTPManager ftpManager;			// reference to an SFTP downloader if that is configured for the integration.
	private String dir;						// absolute path assumed
	private String filePattern;				// specified as a regular expression
	private String integrationId;			// integration id to pass to file job
	private JobLauncher jobLauncher; 		// through the context
	private Job processFile;				// the job that we need to instantiate and run for each file
	private String logId;					// the logId of the
	private IntegrationDAO intDAO;			// integration DAO
	private IntegrationFileDAO intFileDAO; 	// integration file DAO

	private String processedDir;			// where to move files which have been successfully processed

	private AlertManager alerts;			// alert manager to email alerts out.
	private EventManager events;			// event manager fires the extensibility events.
	private ErrorDAO errors;				// error messages come from here
	
	private int failed = 0;
	private int total = 0;
	private int minFileCount;				// minimum file count for the current integration

	private String logFile;					// log file name saved here while redirecting each individual file log

	private int retryCount;					// how many times to retry looking for files (and downloading files)
	private long retrySleep;				// how much time to wait between retries 
	/**
	 * Attempt to download files if the download process is configured for this integration.
	 * 
	 * @return	true if successful (even if no files loaded, false if not successful)
	 */
	protected boolean downloadFiles() {
		// try to download files if this functionality was configured
	
		if( ftpManager != null && ftpManager.isDownloadConfigured()) {
			try {
				if( fireEvent("Before FTP") == EventExit.ERROR_STOP )
					throw new FXException("Before FTP Event failed.");
				
				intDAO.changeLogEntry(logId, "Running", "Downloading files.");
				log.debug("ftpManager is " + ftpManager.toString());
				ftpManager.download(filePattern);

				if( fireEvent("After FTP") == EventExit.ERROR_STOP )
					throw new FXException("After FTP Event failed.");
				
				return true;
			}catch(Exception e) {
				alerts.error("Integration '"+filePattern+"' SFTP failed.", "Could not download files: "+e.getMessage());
				intDAO.changeLogEntry(logId, "Error", "Could not download files for integration "+filePattern+": " +e.getMessage());
				return false;
			}
		}
		else
			return true;
	}

	/**
	 * Fire event helper.
	 * 
	 * @param name	Event name.
	 * @return		true/false - whether or not to continue.
	 */
	protected EventExit fireEvent(String name) {
		EventExit resultBefore = events.fireEvent(name);

		if( resultBefore == EventExit.ERROR_STOP ) { 
			intDAO.changeLogEntry(logId, "Error", name+" event failed. Please see logs for details.");
			alerts.error("ERROR: Integration '"+filePattern+"'", name+" event failed. Please see logs for details.");
			log.error("ERROR: Integration '"+filePattern+"' " + name + "event failed. Please see logs for details.");
		}
		if( resultBefore == EventExit.ERROR_CONTINUE ) {
			intDAO.changeLogEntry(logId, "Error", name+" event failed but execution can continue. Please see logs for details.");
			alerts.error("ERROR: Integration '"+filePattern+"'", name+" event failed but execution can continue. Please see logs for details.");
			log.error("ERROR: Integration '"+filePattern+"' " + name + "event failed but execution can continue. Please see logs for details.");
		}
		return resultBefore;
	}
	
	public void execute(final String intName, final String existingLogId) throws Exception {
		String fileName = intName.replace(" ","_")+"_"+existingLogId+".log";
		this.filePattern = intDAO.lookupIntegrationPattern(intName);
		this.integrationId = intDAO.lookupIntegrationId(intName);
		
		if(existingLogId.equals("none")) {
			this.logId = intDAO.addLogEntry(integrationId, "In Progress", "Looking for files in source folder.");
		}
		else {
			intDAO.changeLogEntry(logId, "In Progress", "Looking for files in source folder.");
			this.logId = existingLogId;
		}
		
		// update the log file name information
		intDAO.updateLogFile(logId, fileName);
		intDAO.updateKillInfo(logId);

		// add permanent substitutions to the event system
		events.addSubst("${ctx.filePattern}", intName);
		events.addSubst("${ctx.integrationId}", integrationId);
		
		alerts.start("START: Integration '"+intName+"'", "The integration has started.");
		
		// fire the before integration event
		EventExit resultBefore = fireEvent("Before Integration");
		if(resultBefore == EventExit.ERROR_STOP)
			return;
		
		// Start the file-retrieval / retry loop. We make sure we have downloaded enough files. If there are
		// not enough files, we use a re-try policy after which we give up and put the integration in Error status.
		
		String [] child = retrieveFiles(dir, filePattern, this.retryCount, this.retrySleep, this.minFileCount);
		if( child == null )
			return;

		// process all the files.
		
		for(int i = 0; i<child.length; i++) {
				log.debug("Trying: "+ "file://"+dir+"/"+child[i]);
				alerts.start("START: File '"+child[i]+"'", 
						"File #"+(i+1)+": '"+child[i]+"' has started processing. There are a total of "+
							child.length + " files to process for integration '"+intName+"'.");
				processFile(child[i]);
				total++;
		}
		
		EventExit resultAfter = events.fireEvent("After Integration");
		if( resultAfter == EventExit.ERROR_STOP )
			return;
		
		if( failed == 0) {
			log.debug("There are "+failed+" failed files for this integration.");
			
			// check the events to see if they failed but allowed continuation.
			if( resultBefore == EventExit.ERROR_CONTINUE || resultAfter == EventExit.ERROR_CONTINUE ) {
				intDAO.changeLogEntry(logId, "Error", "Integration: Before() or After() event failed but was allowed to continue. Please see logs for details.");
				alerts.error("ERROR: Integration '"+intName+"'","Integration: Before() or After() event failed but was allowed to continue. Please see logs for details.");
				log.error("Integration '"+intName+": Before() or After() event failed but was allowed to continue. Please see logs for details.");
			}
			else {
				log.debug("Success: logged to record: "+logId);
				intDAO.changeLogEntry(logId, "Finished", "Processed: "+total+" files.");
				alerts.success("SUCCESS: Integration '"+intName+"'","Processed: "+total+" files.");
			}
		}
		else if( total > failed ) {
			intDAO.changeLogEntry(logId, "Finished with Errors", "Processed: "+total+" files, Failed: "+failed+" files. Please see individual files for details.");
			alerts.error("ERROR: Integration '"+intName+"'","Processed: "+total+" files, Failed: "+failed+" files. Please see individual files for details.");
			log.error("Integration '"+intName+"': Processed: "+total+" files, Failed: "+failed+" files. Please see individual files for details.");
		}
		else {
			intDAO.changeLogEntry(logId, "Error", "All "+ failed + " files failed. Please see individual files for details.");
			alerts.error("ERROR: Integration '"+intName+"'","All "+ failed + " files failed. Please see individual files for details.");
			log.error("ERROR: Integration '"+intName+"': All "+ failed + " files failed. Please see individual files for details.");
		}
	}
	
	/**
	 * Perform the file retrieval loop. 
	 * 
	 * @param dir				- directory to look for 'incoming' files.
	 * @param filePattern		- file pattern to look for (regex)
	 * @param retryCount		- how many times to retry if we are not 
	 * @param retrySleep		- how long (millisecnods) to wait before we retry.
	 * @param minFileCount		- minimum number of files we expect to find in the folder (after we download).
	 * @return list of file names which match the file pattern or null if we failed.
	 * @throws FXNotEnoughFilesException
	 */
	private String[] retrieveFiles(String dir, final String filePattern, int retryCount, long retrySleep, int minFileCount) throws FXNotEnoughFilesException {
		boolean enoughFiles = false;
		String [] child = null;
		
		int currentTryCount = 0;
		
		log.debug("Entering file retrieval loop: dir="+dir+",filePattern="+filePattern+", retryCount="+retryCount+", retrySleep="+retrySleep+", minFileCount="+minFileCount);
		
		while(!enoughFiles) {

			// download any outstanding files
			if(!downloadFiles())
				return null;
	
			// locate files eligible to be processed by this integration.
			File fdir = new File(dir);
			
			child = fdir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					// do the pattern matching on regular expression
					return name.matches(filePattern);
				}
			});
			
			if( child == null ) {
				// can't get to the directory, so quit here.
				intDAO.changeLogEntry(logId, "Error", "Source directory is not accessible or does not exist. Please check global settings and file permissions.");
				log.error("Directory '"+dir+"' is not accessible or does not exist.");
				return null;
			}
			
			 if(child.length==0) {
				 log.debug("There are 0 files in the '"+dir+"' directory.");
			 }
			 
			 if(child.length < minFileCount) {
				 log.debug("We got "+child.length+" files, but expecting "+minFileCount);
				 if( currentTryCount == retryCount ) {
					 log.debug("Current try count exceeded retry. current="+currentTryCount+", retryCount="+retryCount);
					 intDAO.changeLogEntry(logId, "Error", "Found "+child.length+" files, but expecting at least: "+minFileCount +". Giving up after "+currentTryCount+" tries.");
					 log.debug("Found "+child.length+" files, but expecting at least: "+minFileCount +". Giving up after "+currentTryCount+" tries.");
					 throw new FXNotEnoughFilesException("Found "+child.length+" files, but expecting at least: "+minFileCount +". Giving up after "+currentTryCount+" tries.");
				 }
				 else {
					 log.debug("Current try count=" + currentTryCount + 
							 	". We are going to wait and retry after "+retrySleep+" ms.");
					 intDAO.changeLogEntry(logId, "In Progress", 
							 "Found "+child.length+" files, but expecting at least: "+minFileCount +". Will retry after "+retrySleep/1000+" seconds." );
					 currentTryCount++;
					 try {
						Thread.sleep(retrySleep);
					} catch (InterruptedException e) {
						log.error(e.toString());
					}
				 }
			 }
			 else
				 enoughFiles = true;
		}
		
		if(child != null) {
			intDAO.changeLogEntry(logId, "In Progress", "Retrieved "+child.length+" files. Starting processing.");
			log.debug("Successfully returning child[] with "+child.length+" entries.");
		}
		else
			log.error("Child can not be NULL after successful file retrieve. This should never happen.");
	
		return child;
	}

	/**
	 * Create the instance in CX_FINT_FILE of the currently running file. Right off the bat, 
	 * let's do a couple of checks - duplicate file, and empty file.
	 * 
	 * @param fileName
	 * @return
	 */
	protected String createFile(String fileName) {

		Properties ps = new Properties();

		ps.put("INTEGRATION_ID", integrationId);
		log.debug("About to insert: " + fileName);
		ps.put("FILE_NAME", fileName);
		ps.put("START_DT", new Date());
		ps.put("END_DT", "");
		ps.put("STAGE", "Initializing");
		ps.put("STATUS", "Running");
		ps.put("RUN_ID", logId);
		ps.put("OWNER_EMP_ID", intDAO.lookupIntegrationOwner(integrationId));
		
		// existing file in the CX_FINT_FILE table - so this must be a duplicate
		if( intFileDAO.fileExists(fileName)) {
			ps.put("ERROR_DESC", errors.msg("SBL-FINT-0017"));
			ps.put("ERROR_CODE", "SBL-FINT-0017");
			ps.put("END_DT", new Date());
			ps.put("STAGE", "Duplicate Check");
			ps.put("STATUS", "Error");
			
			intFileDAO.insertFile(ps);
			
			return null;
		}
		// 0-length file detected
		else if(new File(dir, fileName).length() == 0) {
			ps.put("ERROR_DESC", errors.msg("SBL-FINT-0018"));
			ps.put("ERROR_CODE", "SBL-FINT-0018");
			ps.put("END_DT", new Date());
			ps.put("STAGE", "Empty File Check");
			ps.put("STATUS", "Error");
			
			intFileDAO.insertFile(ps);
			
			return null;
		}
		else
			return intFileDAO.insertFile(ps);
	}

	// launch a job for the file and update the log.
	private void processFile(String fileName) {
		try {
			intDAO.changeLogEntry(logId, "In Progress", "Processing file "+fileName);
			addLogFile(fileName);
			String fileId = createFile(fileName);

			if(fileId == null) {
				moveFileToProcessed(fileName,"dup_"+logId+".");
				throw new FXException("Duplicate or 0-length file detected.");
			}
			
			JobExecution status = jobLauncher.run(processFile, new JobParametersBuilder().
							addString("fileId", fileId).
							addString("integrationId", integrationId).toJobParameters());

			if(status.getStatus().equals(BatchStatus.FAILED)) {
				failed++;
				log.error("File '"+fileName+"' failed: "+status.getFailureExceptions().toString());
			} 
			
			// check that preValidate completed successfully and then move the file, whatever the status is
			
			for( StepExecution s : status.getStepExecutions()) {
				if( s.getStepName().endsWith("preValidate") ) 
						if(s.getExitStatus().equals(ExitStatus.COMPLETED) ) {
							moveFileToProcessed(fileName,"");
							break;
						}
			}
		}catch(Exception e) {
			intDAO.changeLogEntry(logId, "Error", "Processing file "+fileName);
			failed++;
			log.error("Error while processing '"+fileName+"': "  + e.toString());
		}
		finally {
			restoreLauncherLog();
		}
	}

	public void moveFileToProcessed(String fileName, String prefix) {
		File doneFile = new File(dir,fileName);
		File destDir = new File(processedDir);
		
		if( new File(destDir, doneFile.getName()).exists()) {
			new File(destDir, doneFile.getName()).delete();
		}
		
		if( ! doneFile.renameTo(new File(destDir, prefix+doneFile.getName())))	{
			log.error("Could not move file '"+fileName+"' to directory '"+processedDir);
		}		
	}

	private void addLogFile(String fileName) {
		// create a temporary file appender
		
		// FileAppender fap = (FileAppender)LogManager.getRootLogger().getAppender("file");
		
		// logFile = fap.getFile();
		// log.info("Redirecting log to '"+new File(fap.getFile()).getParent()+File.separatorChar+fileName+".log"+"'");
		
		// fap.setFile(new File(fap.getFile()).getParent()+File.separatorChar+fileName+".log");
		// fap.activateOptions();
	}
	
	private void restoreLauncherLog() {
		/*
		FileAppender fap = (FileAppender)LogManager.getRootLogger().getAppender("file");
		log.info("Redirecting log back to integration log '"+logFile+"'");
		fap.setFile(logFile);
		fap.activateOptions();
		*/
	}

	public String getDir() {
		return dir;
	}

	@Required
	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getFilePattern() {
		return filePattern;
	}

	@Required
	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	public JobLauncher getJobLauncher() {
		return jobLauncher;
	}

	@Required
	public void setJobLauncher(JobLauncher jobLauncher) {
		this.jobLauncher = jobLauncher;
	}

	public Job getProcessFile() {
		return processFile;
	}

	@Required
	public void setProcessFile(Job processFile) {
		this.processFile = processFile;
	}

	public IntegrationDAO getIntDAO() {
		return intDAO;
	}

	@Required
	public void setIntDAO(IntegrationDAO intDAO) {
		this.intDAO = intDAO;
	}

	public String getProcessedDir() {
		return processedDir;
	}

	@Required
	public void setProcessedDir(String processedDir) {
		this.processedDir = processedDir;
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

	public IntegrationFileDAO getIntFileDAO() {
		return intFileDAO;
	}

	@Required
	public void setIntFileDAO(IntegrationFileDAO intFileDAO) {
		this.intFileDAO = intFileDAO;
	}

	public ErrorDAO getErrors() {
		return errors;
	}

	@Required
	public void setErrors(ErrorDAO errors) {
		this.errors = errors;
	}

	public int getMinFileCount() {
		return minFileCount;
	}

	@Required
	public void setMinFileCount(int minFileCount) {
		this.minFileCount = minFileCount;
	}

	public SFTPManager getFtpManager() {
		return ftpManager;
	}

	@Required
	public void setFtpManager(SFTPManager ftpManager) {
		this.ftpManager = ftpManager;
	}

	public int getRetryCount() {
		return retryCount;
	}

	@Required
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public long getRetrySleep() {
		return retrySleep;
	}

	@Required
	public void setRetrySleep(long retrySleep) {
		this.retrySleep = retrySleep;
	}
}
