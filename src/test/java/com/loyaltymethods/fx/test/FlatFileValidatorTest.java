package com.loyaltymethods.fx.test;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.loyaltymethods.fx.file.FlatFileValidator;

@ContextConfiguration(locations={"/FlatFileValidator.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class FlatFileValidatorTest {
	Logger log = Logger.getLogger(FlatFileValidatorTest.class);
	
	@Autowired
	public FlatFileValidator ifm;
	
	@Test
	public void testDatePattern() throws Exception {

		FieldSet fieldSet = new DefaultFieldSetFactory().create(	
				new String [] {"2008/1/10" },			// values
				new String [] {"TEST_DATE_FORMAT"} );	// names

		Map<String, Object> out = ifm.process(fieldSet);
		
		assertTrue(out.get("ERROR_CODE") == null);
		assertTrue(out.get("ERROR_DESC") == null);

		log.debug(out);
	}
	
	@Test
	public void testDefaultDatePattern() throws Exception {

		FieldSet fieldSet = new DefaultFieldSetFactory().create(	
				new String [] {"12/12/2008 23:00:00" },			// values
				new String [] {"TEST_DEFAULT_DATE_FORMAT"} );	// names

		Map<String, Object> out = ifm.process(fieldSet);
		
		assertTrue(out.get("ERROR_CODE") == null);
		assertTrue(out.get("ERROR_DESC") == null);

		log.debug(out);

	}
	
	@Test
	public void testRequired() throws Exception {

		FieldSet fieldSet = new DefaultFieldSetFactory().create(	
				new String [] {" " },			// values
				new String [] {"TEST_REQUIRED"} );		// names

		Map<String, Object> out = ifm.process(fieldSet);
		
		assertTrue(out.get("ERROR_CODE") != null);
		assertTrue(out.get("ERROR_DESC") != null);
		
		log.debug(out);
	}
	
	@Test
	public void testDefault() throws Exception {

		FieldSet fieldSet = new DefaultFieldSetFactory().create(	
				new String [] {"" },				// values
				new String [] {"TEST_DEFAULT_VALUE"} );	// names

		Map<String, Object> out = ifm.process(fieldSet);
		
		assertTrue(out.get("ERROR_CODE") == null);
		assertTrue(out.get("ERROR_DESC") == null);
		assertTrue(out.get("TEST_DEFAULT_VALUE") != null );
		
		log.debug(out);
	}
	
	@Test
	public void testDefaultNumberFormat() throws Exception {

		FieldSet fieldSet = new DefaultFieldSetFactory().create(	
				new String [] {"132.012" },						// values
				new String [] {"TEST_DEFAULT_NUMBER_FORMAT"} );	// names

		Map<String, Object> out = ifm.process(fieldSet);
		
		assertTrue(out.get("ERROR_CODE") == null);
		assertTrue(out.get("ERROR_DESC") == null);
		
		log.debug(out);
	}

	@Test
	public void testNumberFormat() throws Exception {

		FieldSet fieldSet = new DefaultFieldSetFactory().create(	
				new String [] {"132.012312" },						// values
				new String [] {"TEST_NUMBER_FORMAT"} );	// names

		Map<String, Object> out = ifm.process(fieldSet);
		
		assertTrue(out.get("ERROR_CODE") == null);
		assertTrue(out.get("ERROR_DESC") == null);
		
		log.debug(out);
	}

	@Test
	public void testStringFormat() throws Exception {

		FieldSet fieldSet = new DefaultFieldSetFactory().create(	
				new String [] {"EmilSarkissian123" },		// values
				new String [] {"TEST_STRING_FORMAT"} );	// names

		Map<String, Object> out = ifm.process(fieldSet);
		
		assertTrue(out.get("ERROR_CODE") == null);
		assertTrue(out.get("ERROR_DESC") == null);
		
		log.debug(out);
	}

	@Test
	public void testStringLength() throws Exception {

		FieldSet fieldSet = new DefaultFieldSetFactory().create(	
				new String [] {"123" },						// values
				new String [] {"TEST_STRING_LENGTH"} );	// names

		Map<String, Object> out = ifm.process(fieldSet);
		
		assertTrue(out.get("ERROR_CODE") == null);
		assertTrue(out.get("ERROR_DESC") == null);

		fieldSet = new DefaultFieldSetFactory().create(	
				new String [] {"Exceeeeeeeeeeeeeeeding" },	// values
				new String [] {"TEST_STRING_LENGTH"} );	// names

		out = ifm.process(fieldSet);
		
		assertTrue(out.get("ERROR_CODE") != null);
		assertTrue(out.get("ERROR_DESC") != null);
	}

}
