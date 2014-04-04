/**
 * 
 */
package com.loyaltymethods.fx.test.common;

import java.util.List;
import java.util.Map;

/**
 * @author Ravi
 *
 */
public class DBInsert {
	private String insertStatement;
	private List<Map<String, String>> listOfRecords;
	
	public String getInsertStatement() {
		return insertStatement;
	}
	public void setInsertStatement(String insertStatement) {
		this.insertStatement = insertStatement;
	}
	public List<Map<String, String>> getListOfRecords() {
		return listOfRecords;
	}
	public void setListOfRecords(List<Map<String, String>> listOfRecords) {
		this.listOfRecords = listOfRecords;
	}
	@Override
	public String toString() {
		return "DBInsert [insertStatement=" + insertStatement
				+ ", listOfRecords=" + listOfRecords + "]";
	}
	
}
