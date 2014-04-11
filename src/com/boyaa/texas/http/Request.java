package com.boyaa.texas.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

import android.app.Dialog;

import com.boyaa.texas.http.Response.ResponseHandler;

public abstract class Request<T> {
	private final String mUrl;
	private Map<String, String> mHeader;
	private Map<String, String> mParams;
	protected final ResponseHandler<T> mResponseHandler;
	public int mMethod = RequestMethod.GET;
	public Dialog dialog;
	private boolean cancel = false;
	
	public boolean isCancle() {
		return cancel;
	}

	public void cancel() {
		cancel = true;
	}

	private int socketTimeoutMs = 30000;
	
	private static String DEFAULT_PARAMS_ENCODING = "UTF-8";

	public interface RequestMethod {
		int GET = 1;
		int POST = 2;
	}
	
	public Request(String url, Map<String, String> header, Map<String, String> params,
			ResponseHandler<T> handler) {
		this.mUrl = url;
		this.mHeader = header;
		this.mParams = params;
		this.mResponseHandler = handler;
	}

	public abstract Response<T> parseResponse(byte[] data);

	protected void dispatchResponse(T response) {
		// do nothing
		if (mResponseHandler != null)
			mResponseHandler.onSuccess(response);
	}

	protected void dispatchError(Error error) {
		if (mResponseHandler != null)
			mResponseHandler.onError(error);
	}
	
	public int getSocketTimeout() {
		return socketTimeoutMs;
	}
	
	public String getParamsEncoding() {
		return DEFAULT_PARAMS_ENCODING;
	}
	
	public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }
	
	public String getUrl() {
		return mUrl;
	}

	public Map<String, String> getHeaders() {
		return Collections.emptyMap();
	}

	public void setHeaders(Map<String, String> mHeader) {
		this.mHeader = mHeader;
	}

	public Map<String, String> getParams() {
		return mParams;
	}

	public void setParams(Map<String, String> mParams) {
		this.mParams = mParams;
	}

	public ResponseHandler<T> getResponseHandler() {
		return mResponseHandler;
	}
	
	public int getMethod() {
		return mMethod;
	}
	
	public byte[] getBody(){
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }
	
	private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

}
