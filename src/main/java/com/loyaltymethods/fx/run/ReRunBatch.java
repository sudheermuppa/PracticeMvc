package com.loyaltymethods.fx.run;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.loyaltymethods.fx.data.IntegrationDAO;
import com.loyaltymethods.fx.data.IntegrationFileDAO;
import com.loyaltymethods.fx.log.util.FXLogger;

public class ReRunBatch {
	static Logger log = Logger.getLogger(ReRunBatch.class);
	
	public static void main(String argv[]) {
		try {
			ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
			        new String[] {argv[0] + ".xml"});
	
			SimpleJobLauncher jobLauncher = (SimpleJobLauncher) appContext.getBean("jobLauncher");
			Job fileJob = (Job) ((DirectoryJobLauncher)appContext.getBean("dirLauncher")).getProcessFile();
			IntegrationDAO intDAO = (IntegrationDAO)appContext.getBean("integrationDAO");
			IntegrationFileDAO intFileDAO = (IntegrationFileDAO)appContext.getBean("intFileDAO");
			DirectoryJobLauncher dirJobLauncher = (DirectoryJobLauncher)appContext.getBean("dirLauncher");
	
			// redirect log
			
			// set output for the RERUN
			
			// FileAppender fap = (FileAppender)LogManager.getRootLogger().getAppender("file");
			String fileName = "rerun_"+intFileDAO.lookupFileName(argv[1])+".log";
			// FXLogger.setLoggingDiagnosticMessage(fileName.substring(0, fileName.indexOf(".log")));
			FXLogger.setLoggingDiagnosticMessage("rerun_"+argv[0].replace(" ","_")+"_"+argv[1]);
			
			if( System.getProperty("pid") != null)
				log.info("Process: "+System.getProperty("pid"));
			
			// log.debug("Setting log file name: "+new File(fap.getFile()).getParent()+File.separator+fileName);
			
			// setup the log file name
			intFileDAO.setFileId(argv[1]);
			
			// refresh the kill info on the integration run that this file belongs to
			intDAO.updateKillInfo(intFileDAO.getRunId());
			
			// fap.setFile(new File(fap.getFile()).getParent()+File.separator+fileName);
			// fap.activateOptions();
			
			if( System.getProperty("pid") != null)
				log.info("Process: "+System.getProperty("pid"));
			
			// Check command line args
			
			if( argv.length != 2) {
				System.err.println("Syntax: ReRunBatch <IntegrationName> <FileRowId>");
				FXLogger.removeLoggingDiagnosticMessage();
				System.exit(3);
			}
			
			JobExecution status = jobLauncher.run(fileJob, new JobParametersBuilder()
									.addString("fileId", argv[1])
									.addString("integrationId",intDAO.lookupIntegrationId(argv[0])).toJobParameters());

			// move file to processed if it is finished successfully on the preValidate

			for( StepExecution s : status.getStepExecutions()) {
				if( s.getStepName().endsWith("preValidate") ) 
					if(s.getExitStatus().equals(ExitStatus.COMPLETED) ) {
						dirJobLauncher.moveFileToProcessed(fileName,""); // Is this moving log file?
						break;
				}
			}
		}catch(Exception e) {
			log.error(e.toString());
			FXLogger.removeLoggingDiagnosticMessage();
			System.exit(1);
		}
		FXLogger.removeLoggingDiagnosticMessage();
		System.exit(0);
	}
}