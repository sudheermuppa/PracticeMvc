package com.loyaltymethods.fx.meta;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Entry point for meta-data generation.
 * 
 * Syntax is:
 * 
 * metagen <Integration Name>
 * 
 * @author Emil
 *
 */
public class MetaGen {
	static Logger log = Logger.getLogger(MetaGen.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.debug("Started with "+args);
	
		if( args.length != 1) {
			System.err.println("Usage: metagen <Integration Name>");
			System.exit(1);
		}
		
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
		        new String[] {"metagen-context.xml"});
		
		MetaGenerator metagen = (MetaGenerator)appContext.getBean("metaGen");
		
		try {
			// it is expected that metagen will log and re-throw any exceptions.
			metagen.genIntegration(args[0]);
		}catch( Exception e) {
			log.error("Error in MetaGenerator.genIntegration()", e);
			System.exit(1);
		}
	}
}
