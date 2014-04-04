/**
 * 
 */
package com.loyaltymethods.fx.test;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.loyaltymethods.fx.log.util.FXLogger;
import com.loyaltymethods.fx.log.util.NDCMatchFilter;

/**
 * @author Ravi
 *
 */

public class FXLoggerTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(new String[] {"log4j-context.xml"});
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFXLoggerBean() {
		FXLogger fXLogger = FXLogger.getInstance();
		assertNotNull("FXLogger.getInstance() is null", fXLogger);
		assertNotNull("fXLogger.getDestLogFolder() is null", fXLogger.getDestLogFolder());
		assertNotNull("fXLogger.getLayoutPattern() is null", fXLogger.getLayoutPattern());
		assertNotNull("fXLogger.getLog4jConfigurationFile() is null", fXLogger.getLog4jConfigurationFile());
		assertNotNull("fXLogger.getLogFilePrefix() is null", fXLogger.getLogFilePrefix());
		NDCMatchFilter matchFilter = fXLogger.getNdcMatchFilter();
		assertNotNull("fXLogger.getDiagonosticContextMatchFilter() is null", matchFilter);
		assertNotNull("fXLogger.getDiagonosticContextMatchFilter().getNext() is null", matchFilter.getNext());
		assertNotNull("fXLogger.getDiagonosticContextMatchFilter().isAcceptOnMatch() is null", matchFilter.isAcceptOnMatch());
		boolean isExactMatch = matchFilter.isExactMatch();
		if(!isExactMatch) {
			assertNotNull("fXLogger.getDiagonosticContextMatchFilter().getValueToMatch() is null", matchFilter.getValueToMatch());
		}
	}

}
