package com.vimc.ahttp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import android.app.Dialog;

import com.vimc.ahttp.Response.ResponseListener;

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

	private final String mUrl;
	private Map<String, String> mHeaders;  //HTTP 头部
	private Map<String, String> mParams;   //HTTP 参数
	protected final ResponseListener<T> mResponseListener; //HTTP请求回调
	public int mMethod = RequestMethod.GET; // Reuqest Method
	public Dialog dialog;
	private String paramsEncoding = DEFAULT_PARAMS_ENCODING;
	private boolean cancel = false;

	public boolean isCancled() {
		return cancel;
	}

	public void cancel() {
		cancel = true;
	}

	private int soTimeoutMs = 30000; //Response 响应超时时间：30秒

	public interface RequestMethod {
		int GET = 1;
		int POST = 2;
		int HEAD = 3;
	}
	
	public Request(String url, Map<String, String> header, Map<String, String> params, ResponseListener<T> listener) {
		this.mUrl = url;
		this.mHeaders = header;
		this.mParams = params;
		this.mResponseListener = listener;
	}

	/**
	 * 解析返回数据
	 * 
	 * @param data
	 * @return
	 */
	public abstract Response<T> parseResponse(byte[] data);

	protected void dispatchResponse(T response) {
		if (mResponseListener != null) {
			mResponseListener.onSuccess(response);
		}
	}

	protected void dispatchError(Error error) {
		if (mResponseListener != null) {
			mResponseListener.onError(error);
		}
	}

	public int getSoTimeout() {
		return soTimeoutMs;
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

	public Map<String, String> getParams() {
		return mParams;
	}

	public void setParams(Map<String, String> mParams) {
		this.mParams = mParams;
	}

	public ResponseListener<T> getResponseListener() {
		return mResponseListener;
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
