/**
 * 
 */
package com.loyaltymethods.fx.test.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.NDC;
import org.h2.tools.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author Ravi
 *
 */
public class FeedXChangeTest {
	
	private BasicDataSource dataSource;
	
	private BasicDataSource siebelDataSource;
	
	private NamedParameterJdbcTemplate namedParameterJdbcTemplateForBatchDS;
	
	private NamedParameterJdbcTemplate namedParameterJdbcTemplateForSiebelDS;
	
	private String simpleClassName;
	
	private List<DBInsert> listOfDBInserts;
	
	private Map<String, String> additionalArgsMap;
	
	private static ClassPathXmlApplicationContext appContext;
	
	private static Server db;
	
	static {
		FeedXChangeTest.appContext = new ClassPathXmlApplicationContext(new String[] {"test-data.xml"});
	}
	
	public FeedXChangeTest() {
		super();
		this.simpleClassName = this.getClass().getSimpleName();
	}
	
	public void insertAll(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		for (DBInsert dBInsert : listOfDBInserts) {
			for (Map<String, String> aRecord : dBInsert.getListOfRecords()) {
				namedParameterJdbcTemplate.update(dBInsert.getInsertStatement(), aRecord);
			}
		}
	}
	
	@BeforeClass
	public static void startDb() throws Exception {
		db = Server.createTcpServer().start();
	}
	
	@Before public void setUp() {
		FeedXChangeTest feedXChangeTestData = (FeedXChangeTest) getAppContext().getBean(getSimpleClassName());
		this.listOfDBInserts = feedXChangeTestData.getListOfDBInserts();
		this.additionalArgsMap = feedXChangeTestData.getAdditionalArgsMap();
		NDC.push(simpleClassName);
	}
	
	@After public void tearDown() {
		NDC.pop();
	}
	
	@AfterClass
	public static void stopDb() throws Exception {
		db.stop();
	}

	public ClassPathXmlApplicationContext getAppContext() {
		return FeedXChangeTest.appContext;
	}
	
	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplateForBatchDS() {
		return namedParameterJdbcTemplateForBatchDS;
	}
	
	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplateForSiebelDS() {
		return namedParameterJdbcTemplateForSiebelDS;
	}
	
	public JdbcOperations getJdbcTemplateForBatchDS() {
		return this.namedParameterJdbcTemplateForBatchDS.getJdbcOperations();
	}

	public JdbcOperations getJdbcTemplateForSiebelDS() {
		return this.namedParameterJdbcTemplateForSiebelDS.getJdbcOperations();
	}

	public String getSimpleClassName() {
		return simpleClassName;
	}

	public List<DBInsert> getListOfDBInserts() {
		return listOfDBInserts;
	}

	public void setListOfDBInserts(List<DBInsert> listOfDBInserts) {
		this.listOfDBInserts = listOfDBInserts;
	}

	public Map<String, String> getAdditionalArgsMap() {
		return additionalArgsMap;
	}

	public void setAdditionalArgsMap(Map<String, String> additionalArgsMap) {
		this.additionalArgsMap = additionalArgsMap;
	}

	@Autowired
	public void setDataSource(BasicDataSource dataSource) {
		this.dataSource = dataSource;
		this.namedParameterJdbcTemplateForBatchDS = new NamedParameterJdbcTemplate(this.dataSource);
	}

	@Autowired
	public void setSiebelDataSource(BasicDataSource siebelDataSource) {
		this.siebelDataSource = siebelDataSource;
		this.namedParameterJdbcTemplateForSiebelDS = new NamedParameterJdbcTemplate(this.siebelDataSource);
	}
	
}
