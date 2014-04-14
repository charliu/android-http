package com.boyaa.texas.http;

@SuppressWarnings("serial")
public class Error extends Exception {
	public static final int NETWORK_ERROR = 1;
	public static final int SERVER_ERROR = 2;
	public static final int PARSE_ERROR = 3;
	public static final int UNKNOWN_ERROR = 4;

	public String errorDescription;
	public int errorCode;

	public Error(int code) {
		errorCode = code;
		errorDescription = "";
	}

	public Error(int code, String des) {
		super(des);
		errorCode = code;
		errorDescription = des;
	}

	@Override
	public String toString() {
		return "Error [errorDescription=" + errorDescription + ", errorCode=" + errorCode + "]";
	}

}
