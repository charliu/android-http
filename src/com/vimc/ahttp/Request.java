package com.vimc.ahttp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.vimc.ahttp.Response.ResponseListener;

import android.app.Dialog;

/**
 * Request抽象，提供如下功能: 请求信息封装 解析返回数据{@link #parseResponse(byte[])} 分发请求数据结果
 * {@link #dispatchResponse(Object) #dispatchError(Error)}
 * 
 * @author charliu
 * 
 * @param <T>
 */
public abstract class Request<T> {

	private static String DEFAULT_PARAMS_ENCODING = "UTF-8";
	public static final int DEFAULT_SO_TIMEOUT_MS = 40 * 1000; // 默认响应超时时间：40秒

	protected String mUrl;
	protected Map<String, String> mHeaders = new HashMap<String, String>(); // HTTP
																			// 头部
	protected TreeMap<String, String> mParams = new TreeMap<String, String>(); // HTTP
																			// 参数
	protected ResponseListener<T> mResponseListener; // HTTP请求回调
	public RequestMethod requestMethod = RequestMethod.POST; // Reuqest Method
	public Dialog dialog;
	protected String paramsEncoding = DEFAULT_PARAMS_ENCODING; //encode type
	private int soTimeoutMs = DEFAULT_SO_TIMEOUT_MS; // 可通过 setSoTimeout方法设置
	protected boolean cancel = false;

	public enum RequestMethod {
		GET, POST, HEAD
	}
	
	public Request() {
	}

	public Request(Map<String, String> params, ResponseListener<T> listener) {
		addParams(params);
		this.mResponseListener = listener;
	}

	public Request(String url, Map<String, String> headers, Map<String, String> params,
			ResponseListener<T> listener) {
		this.mUrl = url;
		addHeaders(headers);
		addParams(params);
		this.mResponseListener = listener;
	}

	public boolean isCancled() {
		return cancel;
	}

	public void cancel() {
		cancel = true;
	}

	public ResponseListener<T> getmResponseListener() {
		return mResponseListener;
	}

	public void setResponseListener(ResponseListener<T> mResponseListener) {
		this.mResponseListener = mResponseListener;
	}

	/**
	 * 解析返回数据
	 * @param data
	 * @return
	 */
	public abstract Response<T> parseResponse(NetworkResponse response);

	protected void dispatchResponse(T response) {
		if (mResponseListener != null) {
			mResponseListener.onSuccess(response);
		}
	}

	protected void dispatchError(HError error) {
		if (mResponseListener != null) {
			mResponseListener.onError(error);
		}
	}

	public int getSoTimeout() {
		return soTimeoutMs;
	}
	
	/**
	 * 
	 * @param soTimeoutMs 单位毫秒
	 */
	public void setSoTimeout(int soTimeoutMs) {
		this.soTimeoutMs = soTimeoutMs;
	}

	public String getParamsEncoding() {
		return paramsEncoding;
	}

	public void setParamsEncoding(String encoding) {
		paramsEncoding = encoding;
	}

	public String getBodyContentType() {
		return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public String getRequestUrl() {
		if (getParams() == null)
			return mUrl;
		return mUrl + "?" + new String(getBody());
	}

	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	public void setHeaders(Map<String, String> headers) {
		this.mHeaders = headers;
	}

	public void addHeader(String name, String value) {
		this.mHeaders.put(name, value);
	}

	public void addHeaders(Map<String, String> headers) {
		if (headers != null) {
			this.mHeaders.putAll(headers);
		}
	}

	public Map<String, String> getParams() {
		return mParams;
	}

	public void setParams(TreeMap<String, String> mParams) {
		this.mParams = mParams;
	}

	public void addParam(String name, String value) {
		this.mParams.put(name, value);
	}

	public void addParams(Map<String, String> params) {
		if (params != null) {
			mParams.putAll(params);
		}
	}

	public ResponseListener<T> getResponseListener() {
		return mResponseListener;
	}

	public byte[] getBody() {
		Map<String, String> params = getParams();
		if (params != null && params.size() > 0) {
			return encodeParameters(params, getParamsEncoding());
		}
		return null;
	}

	public HttpEntity getPostEntity() {
		UrlEncodedFormEntity formEntity = null;
		if (getParams() != null) {
			List<NameValuePair> postParams = new ArrayList<NameValuePair>();
			for (String key : getParams().keySet()) {
				postParams.add(new BasicNameValuePair(key, getParams().get(key)));
			}
			try {
				formEntity = new UrlEncodedFormEntity(postParams, getParamsEncoding());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return formEntity;
	}

	/**
	 * 编码参数
	 * 
	 * @param params
	 * @param paramsEncoding
	 * @return
	 */
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
