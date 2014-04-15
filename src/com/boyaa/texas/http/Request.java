package com.boyaa.texas.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import android.app.Dialog;

import com.boyaa.texas.http.Response.ResponseHandler;

/**
 * Request抽象，提供如下功能:
 * 请求信息封装
 * 解析返回数据{@link #parseResponse(byte[])}
 * 分发请求数据结果{@link #dispatchResponse(Object) #dispatchError(Error)}
 * @author charliu
 *
 * @param <T>
 */
public abstract class Request<T> {
	private final String mUrl;
	private Map<String, String> mHeaders;
	private Map<String, String> mParams;
	protected final ResponseHandler<T> mResponseHandler;
	public int mMethod = RequestMethod.GET;
	public Dialog dialog;
	private boolean cancel = false;

	public boolean isCancled() {
		return cancel;
	}

	public void cancel() {
		cancel = true;
	}

	private int soTimeoutMs = 30000;

	private static String DEFAULT_PARAMS_ENCODING = "UTF-8";

	public interface RequestMethod {
		int GET = 1;
		int POST = 2;
	}

	public Request(String url, Map<String, String> header, Map<String, String> params, ResponseHandler<T> handler) {
		this.mUrl = url;
		this.mHeaders = header;
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

	public int getSoTimeout() {
		return soTimeoutMs;
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
		return mHeaders;
	}

	public void setHeaders(Map<String, String> headers) {
		this.mHeaders = headers;
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

	public byte[] getBody() {
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
