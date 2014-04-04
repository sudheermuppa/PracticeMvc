package com.loyaltymethods.fx.enums;

/**
 * Enum for Integration File Status
 * 
 * @author Ravi
 *
 */
public enum IntegrationFileStatus {
	RUNNING("Running"), QUEUED("Queued"), COMPLETE("Complete"), PARTIALLY_COMPLETE("Partially Complete"), ERROR("Error");
	 
	private String status;
 
	private IntegrationFileStatus(String s) {
		status = s;
	}
 
	public String getStatus() {
		return status;
	}
}
