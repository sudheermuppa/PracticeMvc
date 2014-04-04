package com.loyaltymethods.fx.test;


import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.loyaltymethods.fx.ex.FXInvalidNumberException;

public class NumberFormatTest {
	Logger log = Logger.getLogger(NumberFormatTest.class);
	@Test
	public void test() throws ParseException {
		DecimalFormat f = new DecimalFormat("$##");
		String r = "$1222.1";
		ParsePosition pp = new ParsePosition(0);
		
		Number num;
		num = f.parse(r,pp);

		if( pp.getIndex() != r.length()) {
			log.debug("Could not parse it...");
		}
		else
			log.debug("Parsed it.");

		
	
	}

}
