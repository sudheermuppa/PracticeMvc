package com.loyaltymethods.fx.run;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.loyaltymethods.fx.meta.PasswordManager;

/**
 * This is so we can retrieve encrypted parameters, or any other parameters
 * from the batch.properties for user events.
 * 
 * @author Emil
 *
 */
public class RunGetParam {
	Logger log = Logger.getLogger(RunGetParam.class);

	public static void main(String[] args) {

		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
		        new String[] {"paramread-context.xml"});

		// of course, an ApplicationContext is just a BeanFactory
		
			PasswordManager passwordManager = (PasswordManager) appContext.getBean("passwordManager");
			System.out.print(passwordManager.getNamedPassword(args[0]));
			System.exit(0);
	}
}
