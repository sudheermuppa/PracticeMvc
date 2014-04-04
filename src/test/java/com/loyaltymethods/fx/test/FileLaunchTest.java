package com.loyaltymethods.fx.test;

import java.util.Date;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.loyaltymethods.fx.data.IntegrationFileDAO;

// things to configure;
@ContextConfiguration(locations={"/Hertz 1.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class FileLaunchTest {
	Logger log = Logger.getLogger(FileLaunchTest.class);

//	@Autowired
//	private Job TxnImport;

	@Autowired
	private Job EIMImport;

	// things to configure;
	private final String FILE_NAME = "Hertz_1.txt";
	private final String INT_ID = "1-37KD5";
	
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private DataSource siebelDataSource;

	@Autowired
	private IntegrationFileDAO intFile;
	
	
	@Test
	public void testLaunchJob() throws Exception {
		Runtime.getRuntime().exec("cmd.exe /c \"copy c:\\data\\partner_in_ftp\\"+FILE_NAME+" c:\\data\\incoming\"").waitFor();
		
		Properties ps = new Properties();

		ps.put("INTEGRATION_ID", INT_ID);
		ps.put("FILE_NAME", FILE_NAME);
		ps.put("START_DT", new Date());
		ps.put("END_DT", "");
		ps.put("STAGE", "Initializing");
		ps.put("STATUS", "Running");
		ps.put("RUN_ID", "<Test>");
		ps.put("OWNER_EMP_ID", "<Test>");
		
		String fileId = intFile.insertFile(ps);
		
		jobLauncher.run(EIMImport, new JobParametersBuilder().
									addString("fileId",fileId).
									addString("integrationId",INT_ID).
									toJobParameters());
	}

	@Test
	public void testReLaunchJob() throws Exception {
		JdbcTemplate dbt = new JdbcTemplate(siebelDataSource);
		
		//dbt.execute("UPDATE CX_FINT_TXN SET REC_STATUS = 'Unfixable' WHERE REC_STATUS='Error'");
		
		jobLauncher.run(EIMImport, new JobParametersBuilder().
									addString("fileId",dbt.queryForObject("SELECT ROW_ID FROM CX_FINT_FILE WHERE FILE_NAME='"+FILE_NAME+"'",String.class)).
									addString("integrationId",INT_ID).
									toJobParameters());
	}
}
