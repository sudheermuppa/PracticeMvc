package com.loyaltymethods.fx.run;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.loyaltymethods.fx.data.ErrorDAO;
import com.loyaltymethods.fx.data.IntegrationFileDAO;
import com.loyaltymethods.fx.ex.FXFileAlreadyProcessedException;

public class PurgeFile {
	static Logger log = Logger.getLogger(PurgeFile.class);

	/**
	 * Syntax: PurgeFile <Integration Name> <fileId>
	 * 
	 * @param args
	 */

	public static void main(String[] args) {
		
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
		        new String[] {args[0]+".xml"});
			
			// of course, an ApplicationContext is just a BeanFactory
			log.debug("PurgeFile with Arguments: "+args[0]+", "+args[1]);
			ErrorDAO errors = (ErrorDAO)appContext.getBean("errorManager");
		
			try {
				log.debug("Obtaining reference to intFile");
				IntegrationFileDAO intFile = (IntegrationFileDAO)appContext.getBean("intFileDAO");
				log.debug("Puring file with id="+args[1]);
				intFile.purgeFile(args[1]);
			}
			catch(FXFileAlreadyProcessedException e) {
				writeError(errors.msg("SBL-FINT-0018"), args[1]);
				System.exit(1);
			}
			catch(Exception e) {
				writeError(e.toString(), args[1]);
				System.exit(1);
			}
			log.debug("Exiting with Success.");
			System.exit(0);
	}
	
	static void writeError(String e, String fileId) {
		log.error("Purge failed: " + e.toString());
		PrintWriter err;
		try {
			log.debug("Writing out the error to file '"+"purge-" + fileId + ".err'");
			err = new PrintWriter(new FileWriter("purge-" + fileId + ".err", false));
			log.debug("Writing error string.");
			err.write(e.toString());
			log.debug("Closing error file.");
			err.close();				
		} catch (IOException e1) {
			log.error("Unable to write error: "+e1.toString());
		}
		log.debug("Exiting with Error.");
	}
}
