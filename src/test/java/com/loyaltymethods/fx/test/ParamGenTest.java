package com.loyaltymethods.fx.test;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.loyaltymethods.fx.meta.ParamGenerator;
import com.loyaltymethods.fx.test.common.FeedXChangeTest;

@ContextConfiguration(locations={"/metagen-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class ParamGenTest extends FeedXChangeTest {
	Logger log = Logger.getLogger(ParamGenTest.class);
	
	@Autowired
	ParamGenerator paramGen;
	
	@Before public void setUp() {
		super.setUp();
		insertAll(getNamedParameterJdbcTemplateForSiebelDS());
	}
	
	@Test
	public void test() throws Exception {
		paramGen.genParams();
		
		// Check to see if build.properties file is created with the properties that were inserted into CX_FINT_SETUP in setUP() method.
		Properties props = new Properties();
		props.load(new FileInputStream(getAdditionalArgsMap().get("PATH_TO_GENERATED_BUILD_PROPERTIES_FILE")));
		assertEquals(getAdditionalArgsMap().get("VALUE_TO_BE_COMPARED"), props.get("test.property"));
	}

	@After public void tearDown() {
		getJdbcTemplateForSiebelDS().update("delete from CX_FINT_SETUP");
		super.tearDown();
	}
}
