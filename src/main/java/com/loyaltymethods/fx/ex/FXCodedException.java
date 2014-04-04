package com.loyaltymethods.fx.ex;

public class FXCodedException extends FXException {

	private static final long serialVersionUID = 1L;

	private String code;
	
	public FXCodedException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
