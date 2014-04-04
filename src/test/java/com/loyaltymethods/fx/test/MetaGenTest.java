package com.loyaltymethods.fx.test;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.loyaltymethods.fx.meta.MetaGenerator;
import com.loyaltymethods.fx.test.common.FeedXChangeTest;

@ContextConfiguration(locations={"/metagen-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class MetaGenTest extends FeedXChangeTest {
	Logger log = Logger.getLogger(MetaGenTest.class);
	
	@Autowired
	MetaGenerator metaGen;
	
	
	@Before public void setUp() {
		super.setUp();
		insertAll(getNamedParameterJdbcTemplateForSiebelDS());
	}
	
	@Test
	public void test() throws Exception {
		metaGen.genIntegration("OSTAR");
	}


	@After public void tearDown() {
		getJdbcTemplateForSiebelDS().update("delete from CX_FINT_INTG");
		super.tearDown();
	}
}
