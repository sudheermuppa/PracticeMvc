package com.loyaltymethods.fx.test;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.loyaltymethods.fx.step.DynSQLStep;

@ContextConfiguration(locations={"/DynSQLTest.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class DynSQLTest {
	
	@Autowired
	protected DynSQLStep step;

	@Test
	public void test() throws Exception {
		step.execute(null,null);
	}
}
