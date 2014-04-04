package com.loyaltymethods.fx.meta;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ParamGen {

	static Logger log = Logger.getLogger(ParamGen.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.debug("Started with "+args);
	
		if( args.length != 0) {
			System.err.println("Usage: paramgen");
			System.exit(1);
		}
		
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
		        new String[] {"metagen-context.xml"});
		
		ParamGenerator paramGen = (ParamGenerator)appContext.getBean("paramGen");
		
		try {
			// it is expected that metagen will log and re-throw any exceptions.
			paramGen.genParams();
		}catch( Exception e) {
			log.error(e.toString());
			System.exit(1);
		}
	}
}
