package com.loyaltymethods.fx.run;

import java.io.File;

import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.loyaltymethods.fx.ex.FXNotEnoughFilesException;
import com.loyaltymethods.fx.log.util.FXLogger;

/**
 * Runs an integration.
 * 
 * Call Syntax:
 * 
 * java RunBatch <IntegrationName> <LogId>
 * 
 * We expect that the LogId is the ROW_ID of the Run which was started for this integration by Siebel.
 * If LogId="none" then we create a new log id from the dirLauncher code.
 * 
 * */

public class RunBatch {
	static Logger log = Logger.getLogger(RunBatch.class);
	
	public static void main(String argv[]) {
		
		// set output for the RUN
		
		//FileAppender fap = (FileAppender)LogManager.getRootLogger().getAppender("file");
		// String fileName = argv[0].replace(" ","_")+"_"+argv[1]+".log";
		FXLogger.setLoggingDiagnosticMessage(argv[0].replace(" ","_")+"_"+argv[1]);
		if( System.getProperty("pid") != null)
			log.info("Process: "+System.getProperty("pid"));
		
		// log.debug("Setting log file name: "+new File(fap.getFile()).getParent()+File.separator+fileName);
		
		// fap.setFile(new File(fap.getFile()).getParent()+File.separator+fileName);
		// fap.activateOptions();
		
		// Check command line args
		log.debug("Runbatch called with "+argv[0].toString());
		if( argv.length != 2) {
			System.err.println("Syntax: RunBatch <IntegrationName> <LogId>");
			log.error("RunBatch called with incorrect number of arguments: "+argv.length+" Args: "+argv.toString());
			System.exit(3);
		}
		
		log.debug("Running with argument: <"+argv[0]+ "> <" + argv[1] + ">");
		
		if( System.getProperty("pid") != null)
			log.info("Process: "+System.getProperty("pid"));
		
			try {
				ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
			        new String[] {argv[0] + ".xml"});
				// of course, an ApplicationContext is just a BeanFactory
				DirectoryJobLauncher dirLauncher = (DirectoryJobLauncher) appContext.getBean("dirLauncher");
				
				try {
					dirLauncher.execute(argv[0], argv[1]);
				}catch(FXNotEnoughFilesException e) {
					log.error(e.toString());
					// exit 2 = retry later
					System.exit(2);
				}
		}catch(Exception e) {
			log.error("Error in main(String argv[]) method", e);
			System.exit(1);
		}
		FXLogger.removeLoggingDiagnosticMessage();
		System.exit(0);
	}
}
