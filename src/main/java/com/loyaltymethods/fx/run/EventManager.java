package com.loyaltymethods.fx.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 
 * Event manager deals with the execution of command line events and passing relevant 
 * parameters.
 * 
 * Event manager is called upon from well-defined execution points in the system in order
 * to allow extensibility within the framework.
 * 
 * It will do some dynamic parameter substitutions (due to lack of step scope), but most
 * parameters will be available from the properties place holders.
 * 
 * @author Emil
 *
 */
public class EventManager {
	Logger log = Logger.getLogger(EventManager.class);
	
	private Map<String, List<String>> eventMap;
	private Map<String, String> subst;					// dynamic substitutions

	public enum EventExit {
		SUCCESS,			// everything went fine
		ERROR_CONTINUE,		// there was a partial problem, with some records, but we can continue
		ERROR_STOP			// there was an unrecoverable error and we should abort the file
	}
	
	/**
	 * Expand the command line parameters which are not handled by the property configurer.
	 * All we are doing right now is supplying the fileId.
	 * 
	 * @param cmdLine
	 * @return
	 */
	String expandParams(String cmdLine, Map<String, String> subst) {
		for( String key : subst.keySet()) {
			cmdLine = cmdLine.replace(key, subst.get(key));
		}

		return cmdLine;
	}
	
	/**
	 * Run a command line and translate the exit code to mean:
	 * 
	 * 0 - SUCCESS
	 * 1 - ERROR_CONTINUE
	 * 2 - ERROR_STOP
	 * 
	 * @param cmdLine
	 * @return
	 */
	EventExit runCmdLine(String cmdLine) {

		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		
		log.debug("Event Command: "+cmdLine);
		
		try {
			pr = rt.exec(cmdLine);
		} catch (IOException e) {
			log.error("Execution for Command line '"+cmdLine+"' failed: \n"+e.toString());
			return EventExit.ERROR_STOP;
		}
		
		// now read in the standard output
		BufferedReader stdOut = new BufferedReader(new InputStreamReader(pr.getInputStream()));

		String line;
		StringBuffer output = new StringBuffer();
		
		log.debug("Reading output for event command");
		try {
			while((line = stdOut.readLine())!= null) {
				output.append(line+"\n");
			}
		} catch (IOException e) {
			log.error("Output stream read for Command line '"+cmdLine+"' failed: \n"+e.toString());
		}
		finally {
			log.debug("STDOUT['"+cmdLine+"'] = \n"+output.toString());
		}

		BufferedReader stdErr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
		StringBuffer error = new StringBuffer();
		
		log.debug("Reading error for event command");
		
		try {
			while((line = stdErr.readLine())!= null) {
				error.append(line+"\n");
			}
		} catch (IOException e) {
			log.error("Error stream read for Command line '"+cmdLine+"' failed: \n"+e.toString());
		}
		finally {
			log.debug("STDERR['"+cmdLine+"'] = \n"+error.toString());
		}
		
		int val = 0;
		try {
			log.debug("Waiting for event command to exit");
			val = pr.waitFor();
		} catch (InterruptedException e) {
			val = 2; // stop
			log.error("Wait for Command line '"+cmdLine+"' failed: \n"+e.toString());
		}
		
		switch(val) {
			case 0:
				log.debug("Successful event.");
				return EventExit.SUCCESS;
			case 1: 
				log.error("STDERR['"+cmdLine+"'] = \n"+error.toString());
				log.error("STDOUT['"+cmdLine+"'] = \n"+output.toString());
				return EventExit.ERROR_CONTINUE;
			default: 
				log.error("STDERR['"+cmdLine+"'] = \n"+error.toString());
				log.error("STDOUT['"+cmdLine+"'] = \n"+output.toString());
				return EventExit.ERROR_STOP; 
		}
	}
	
	/**
	 * Fire an event of a given type - go through the eventMap and run all the command lines
	 * specified. Capture the exit codes.
	 * 
	 * @param type
	 * @return
	 */
	public EventExit fireEvent(String type) {
		EventExit result = EventExit.SUCCESS;
		log.debug("Fire Event: "+type);
		List<String> list = eventMap.get(type);
		if( list != null ) {
			for( String cmdLine : list) {
				cmdLine = expandParams(cmdLine, this.subst);
				result = runCmdLine(cmdLine);
				if( result == EventExit.ERROR_STOP )
					return result;
			}
		}
		return result;
	}
	
	/**
	 * Variation on fireEvent where we are able to pass a local context
	 * which is only valid throughout the event lifetime.
	 * 
	 * @param type
	 * @param localCtx
	 * @return
	 */
	public EventExit fireEvent(String type, Map<String,String> localCtx) {
		EventExit result = EventExit.SUCCESS;

		List<String> list = eventMap.get(type);
		if( list != null ) {
			for( String cmdLine : list) {
				cmdLine = expandParams(cmdLine, this.subst);
				cmdLine = expandParams(cmdLine, localCtx);
				result = runCmdLine(cmdLine);
				if( result == EventExit.ERROR_STOP )
					return result;
			}
		}
		return result;
	}
	
	public Map<String, List<String>> getEventMap() {
		return eventMap;
	}

	public void setEventMap(Map<String, List<String>> eventMap) {
		this.eventMap = eventMap;
	}
	
	public void addSubst(String field, String value) {
		if( subst == null )
			subst = new HashMap<String, String>();
		
		subst.put(field, value);
	}
}
