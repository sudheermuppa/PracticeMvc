package com.loyaltymethods.fx.test;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughFieldExtractor;

public class FormatterTest {

	@Test
	public void test() {
		FormatterLineAggregator<String[]> agg = new FormatterLineAggregator<String []>();
		agg.setFormat("%s,%s,%s,%s");
		agg.setFieldExtractor(new PassThroughFieldExtractor<String []>());
		System.out.println(agg.aggregate(new String [] {"Emil","","Agop","Sarkissian"}));
		
		//System.out.format("%1$td/%1$tm/%1$tY", new Date());
		//System.out.println(new Date());
		String s = "  Expr:something";
		System.out.println(s.substring(s.indexOf("Expr:")+"Expr:".length()));
	}
}
