package com.loyaltymethods.fx.test;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.loyaltymethods.fx.run.DirectoryJobLauncher;

@ContextConfiguration(locations={"/Hertz 1.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class IntegrationLaunchTest {

	@Autowired
	DirectoryJobLauncher dirLauncher;
	
	@Test
	public void test() throws Exception {
		dirLauncher.execute("Hertz 1","none");
	}
}
