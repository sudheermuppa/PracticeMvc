package com.loyaltymethods.fx.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.loyaltymethods.fx.data.ErrorDAO;

@ContextConfiguration(locations={"/ErrorMessageTests.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class ErrorMessageTests {
	Logger log = Logger.getLogger(ErrorMessageTests.class);
	
	@Autowired
	ErrorDAO errors;
	
	@Test
	public void test() {
		assertTrue((errors.msg("SBL-FINT-0006")).equals("B - Member '%1' ID Invalid '%2' or missing"));
		assertTrue(errors.msg("SBL-FINT-0006","Hey there").equals("B - Member 'Hey there' ID Invalid '%2' or missing"));
		assertTrue(errors.msg("SBL-FINT-0006","Emil","Something").equals("B - Member 'Emil' ID Invalid 'Something' or missing"));
	}
}

