/**
 * 
 */
package com.loyaltymethods.fx.log.util;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Configure and initialize the log4j.
 * 
 * @author Ravi
 *
 */
public class FXLogger {
	private static FXLogger fxLogger;
	static Logger log = null;
	private String log4jConfigurationFile;
	private String layoutPattern;
	private String destLogFolder;
	private String logFilePrefix;
	private NDCMatchFilter ndcMatchFilter;
	private LoggerMatchFilter loggerMatchFilter;
	
	private FXLogger() {
		super();
	}

	public static FXLogger getInstance() {
		if(FXLogger.fxLogger == null) {
			new ClassPathXmlApplicationContext(new String[] {"log4j-context.xml"});
		}
		return FXLogger.fxLogger;
	}
	
	public void onInit() {
		if(FXLogger.fxLogger == null) {
			DOMConfigurator.configure(log4jConfigurationFile);
			log = Logger.getLogger(FXLogger.class);
			log.info("Log4j configuration completed successfully!");
			FXLogger.fxLogger = this;
		}
	}
	
	public void onDestroy() {
		// Can be used to archive log files...
	}
	
	/**
	 * Retrieve a logger named according to the value of the <code>name</code>
	 * parameter. If the named logger already exists, then the existing instance
	 * will be returned. Otherwise, a new instance is created by configuring 
	 * an appender with the supplied LoggerMatchFilter configuration.
	 * 
	 * @param name
	 *            The name of the logger to retrieve.
	 */
	public static Logger getLogger(String name) {
		FXLogger fxLogger = FXLogger.getInstance();
		if(fxLogger == null || fxLogger.getLoggerMatchFilter() == null) {
			// The LoggerMatchFilter configuration not found. So skip filtering...
			log.error("Missing LoggerMatchFilter configuration in log4j-context.xml");
			return Logger.getLogger(name);
		}
		Logger rootLogger = Logger.getRootLogger();
		String loggerToMatch = null;
		if(fxLogger.getNdcMatchFilter().isExactMatch()) {
			loggerToMatch = name;
		} else {
			// read the 'loggerToMatch' from configuration
			loggerToMatch = fxLogger.getLoggerMatchFilter().getLoggerToMatch();
		}		
		// Check to see if an appender exist for given filtering criteria.
		if(rootLogger.getAppender(loggerToMatch) != null) {
			// An appender with LoggerMatchFilter already exist.
			return Logger.getLogger(name);
		}		
		// An appender don't exist for given NDC, so add new one...
		try {
			LoggerMatchFilter matchFilter = fxLogger.getLoggerMatchFilter().clone();
			if(matchFilter.isExactMatch()) {
				matchFilter.setLoggerToMatch(name);
			}
			String newLogFilePath = fxLogger.getDestLogFolder() + fxLogger.getLogFilePrefix() +loggerToMatch+".log";
			try {
				PatternLayout patternLayout = new PatternLayout(fxLogger.getLayoutPattern());
				FileAppender fileAppender = new FileAppender(patternLayout, newLogFilePath);
				fileAppender.setName(loggerToMatch);		
				fileAppender.setAppend(true);
				fileAppender.addFilter(matchFilter);
				
				rootLogger.addAppender(fileAppender);
				log.info("Successfully configured new appender with log file = " + newLogFilePath + ", fxLogger = " + fxLogger);
			} catch (IOException e) {
				log.error("Error in FXLogger.getLogger() while configuring new appender with log file = " + newLogFilePath + ", fxLogger = " + fxLogger, e);
			}					
		} catch (CloneNotSupportedException e1) {
			log.error("Error in FXLogger.getLogger() method while getting copy of LoggerMatchFilter instance from fxLogger = " + fxLogger, e1);
		}
	
		return Logger.getLogger(name);
	}
	
	/**
	 * Shorthand for <code>getLogger(clazz.getName())</code>.
	 * 
	 * @param clazz
	 *            The name of <code>clazz</code> will be used as the name of the
	 *            logger to retrieve. See {@link #getLogger(String)} for more
	 *            detailed information.
	 */
	public static Logger getLogger(Class clazz) {
		return FXLogger.getLogger(clazz.getName());
	}
	  
	/**
	 * Push new diagnostic context information for the current thread
	 *  to distinguish interleaved log output from different sources.
	 * 
	 * <p>
	 * The contents of the <code>message</code> parameter is determined solely
	 * by the caller.
	 * 
	 * @param message
	 *            The new diagnostic context information.
	 */
	public static void setLoggingDiagnosticMessage(String message) {
		FXLogger fxLogger = FXLogger.getInstance();
		if(fxLogger == null || fxLogger.getNdcMatchFilter() == null) {
			// The NDCMatchFilter configuration not found. So skip filtering...
			log.error("Missing NDCMatchFilter configuration in log4j-context.xml");
			NDC.push(message);
			return;
		}
		Logger rootLogger = Logger.getRootLogger();
		String valueToMatch = null;
		if(fxLogger.getNdcMatchFilter().isExactMatch()) {
			valueToMatch = message;
		} else {
			// read the 'valueToMatch' from configuration
			valueToMatch = fxLogger.getNdcMatchFilter().getValueToMatch();
		}		
		// Check to see if an appender exist for given filtering criteria.
		if(rootLogger.getAppender(valueToMatch) != null) {
			// An appender with NDCMatchFilter already exist.
			NDC.push(message);
			return;	
		}		
		// An appender don't exist for given NDC, so add new one...
		try {
			NDCMatchFilter matchFilter = fxLogger.getNdcMatchFilter().clone();
			if(matchFilter.isExactMatch()) {
				String parentNDC = NDC.get();
				if(parentNDC != null) {
					matchFilter.setValueToMatch(parentNDC + " " + message);
				} else {
					matchFilter.setValueToMatch(message);
				}
			}
			String newLogFilePath = fxLogger.getDestLogFolder() + fxLogger.getLogFilePrefix() +valueToMatch+".log";
			try {
				PatternLayout patternLayout = new PatternLayout(fxLogger.getLayoutPattern());
				FileAppender fileAppender = new FileAppender(patternLayout, newLogFilePath);
				fileAppender.setName(valueToMatch);		
				fileAppender.setAppend(true);
				fileAppender.addFilter(matchFilter);
				
				rootLogger.addAppender(fileAppender);
				log.info("Successfully configured new appender with log file = " + newLogFilePath + ", fxLogger = " + fxLogger);
			} catch (IOException e) {
				log.error("Error in FXLogger.setDiagonosticContext() while configuring new appender with log file = " + newLogFilePath + ", fxLogger = " + fxLogger, e);
			}					
		} catch (CloneNotSupportedException e1) {
			log.error("Error in FXLogger.setDiagonosticContext() method while getting copy of NDCMatchFilter instance from fxLogger = " + fxLogger, e1);
		}
		NDC.push(message);		
	}
	
	/**
	 * Caller to "setLoggingDiagnosticMessage(String message)" method should 
	 * call this method before leaving a diagnostic context.
	 */
	public static void removeLoggingDiagnosticMessage() {
		NDC.pop();
	}

	public String getLog4jConfigurationFile() {
		return log4jConfigurationFile;
	}

	public void setLog4jConfigurationFile(String log4jConfigurationFile) {
		this.log4jConfigurationFile = log4jConfigurationFile;
	}

	public String getLayoutPattern() {
		return layoutPattern;
	}

	public void setLayoutPattern(String layoutPattern) {
		this.layoutPattern = layoutPattern;
	}

	public String getDestLogFolder() {
		return destLogFolder;
	}

	public void setDestLogFolder(String destLogFolder) {
		this.destLogFolder = destLogFolder;
	}

	public String getLogFilePrefix() {
		return logFilePrefix;
	}

	public void setLogFilePrefix(String logFilePrefix) {
		this.logFilePrefix = logFilePrefix;
	}

	public NDCMatchFilter getNdcMatchFilter() {
		return ndcMatchFilter;
	}

	public void setNdcMatchFilter(NDCMatchFilter ndcMatchFilter) {
		this.ndcMatchFilter = ndcMatchFilter;
	}

	public LoggerMatchFilter getLoggerMatchFilter() {
		return loggerMatchFilter;
	}

	public void setLoggerMatchFilter(LoggerMatchFilter loggerMatchFilter) {
		this.loggerMatchFilter = loggerMatchFilter;
	}

	public static void main(String[] args) {
		FXLogger.setLoggingDiagnosticMessage("Parent");
		log.info("Here is log msg for parent log");
		for(int i = 1; i <= 5; i++) {
			long time2 = new Date().getTime();
			Thread newThread = new Thread(new Runnable() {
				long time = new Date().getTime();
				public void run() {
					FXLogger.setLoggingDiagnosticMessage("MyNDC#" + time);
					log.info("Here is log msg for MyNDC#" + time);
					FXLogger.removeLoggingDiagnosticMessage();
				}
			}, "Thread#" + time2);
			newThread.run();
		}
		FXLogger.removeLoggingDiagnosticMessage();
		log.info("Testing LoggerMatchFilter...");
		Logger parentLogger = FXLogger.getLogger("ParentLogger");
		parentLogger.info("Here is log msg for parent log");
		for(int i = 1; i <= 5; i++) {
			long time2 = new Date().getTime();
			Thread newThread = new Thread(new Runnable() {
				long time = new Date().getTime();
				public void run() {
					Logger childLogger = FXLogger.getLogger("Child_Logger" + time);
					childLogger.info("Here is log msg for MyNDC#" + time);
					FXLogger.removeLoggingDiagnosticMessage();
				}
			}, "Thread#" + time2);
			newThread.run();
		}		
	}

	@Override
	public String toString() {
		return "FXLogger [log4jConfigurationFile=" + log4jConfigurationFile
				+ ", layoutPattern=" + layoutPattern + ", destLogFolder="
				+ destLogFolder + ", logFilePrefix=" + logFilePrefix
				+ ", ndcMatchFilter=" + ndcMatchFilter + ", loggerMatchFilter="
				+ loggerMatchFilter + "]";
	}
}
