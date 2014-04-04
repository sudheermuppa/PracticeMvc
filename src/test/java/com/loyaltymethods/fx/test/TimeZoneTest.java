package com.loyaltymethods.fx.test;

import static org.junit.Assert.*;

import java.util.TimeZone;

import org.junit.Test;

public class TimeZoneTest {

	@Test
	public void test() {
		
		System.out.println("emil.zip".replaceAll("."+"zip","."+"ack"));
		
		if( "OSO".matches("OST|POR|ZOR") )
			System.out.println("Matches");
		else
			System.out.println("Not Matching");
		
		String s = "RegEx:something else";
		System.out.println(s.substring("RegEx:".length()));
		
//	    String[] ids = TimeZone.getAvailableIDs();
//	    for (String id : ids) {
//	      System.out.println(id);
//	    }
//	    
//	    System.out.println("Default: "+TimeZone.getDefault().getID());
	}

}
