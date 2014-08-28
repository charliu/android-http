package com.vimc.ahttp;

/**
 * 
 * @author CharLiu
 * 
 */
@SuppressWarnings("serial")
public class HError extends Throwable {
	public static final int NETWORK_ERROR = -1;
	public static final int SERVER_ERROR = -2;
	public static final int PARSE_ERROR = -3;
	public static final int UNKNOWN_ERROR = -4;

	protected String errorMsg;
	protected int errorCode;

	public HError(Throwable t, int code, String msg) {
		super(t);
		this.errorCode = code;
		this.errorMsg = msg;
	}

	public HError(Throwable t, int code) {
		super(t);
		this.errorCode = code;
		this.errorMsg = "";
	}
	
	public HError(int code) {
		this.errorCode = code;
		this.errorMsg = "";
	}

	public HError(int code, String msg) {
		this.errorCode = code;
		this.errorMsg = msg;
	}

	public String getErrorMsg() {
		return errorMsg + "ErrorCode:" + errorCode + " Stack:" + getMessage();
	}
	
	public int getErrorCode() {
		return errorCode;
	}

}