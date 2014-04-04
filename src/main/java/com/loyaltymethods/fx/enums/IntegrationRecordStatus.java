package com.loyaltymethods.fx.enums;

/**
 * Enum for Integration Record Status
 * 
 * @author Ravi
 *
 */
public enum IntegrationRecordStatus {
	PRESTAGED("Prestaged"), QUEUED("Queued"), PROCESSED("Processed"), LOADED("Loaded"), ERROR("Error");
	 
	private String status;
 
	private IntegrationRecordStatus(String s) {
		status = s;
	}
 
	public String getStatus() {
		return status;
	}
}
