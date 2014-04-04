package com.loyaltymethods.fx.test;
import static org.junit.Assert.*;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.loyaltymethods.fx.data.LOVCacheDAO;

@ContextConfiguration(locations={"/Hertz_Accruals.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class LOVCacheTests {
	
	@Autowired
	DataSource siebelDataSource;
	
	@Test
	public void test() {
		LOVCacheDAO lovCache = new LOVCacheDAO();
		lovCache.setSblDS(siebelDataSource);
		try {
			lovCache.afterPropertiesSet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lovCache.registerType("AAG_SVC_TYPE", "VAL");
		lovCache.registerType("AAG_SVC_TYPE", "NAME");
		
		lovCache.loadCache();
		
	}

}
