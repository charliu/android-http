package com.boyaa.texas.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Use HttpClient execute an request, return HttpReqponse 
 * @author CharLiu
 *
 */
public class HttpClientWorker implements HttpWorker {

	protected final HttpClient mClient;

	public HttpClientWorker(HttpClient client) {
		this.mClient = client;
	}

	@Override
	public HttpResponse doHttpRquest(Request<?> request) throws IOException {
		HttpUriRequest httpRequest = createUriRequest(request);
		addRequestHeader(httpRequest, request.getHeaders());
		HttpParams httpParams = httpRequest.getParams();
		int timeoutMs = request.getSoTimeout();
		HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
		HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);

		return mClient.execute(httpRequest);
	}

	private void addRequestHeader(HttpUriRequest request, Map<String, String> headers) {
		if (headers != null) {
			for (String key : headers.keySet()) {
				request.addHeader(key, headers.get(key));
			}
		}
	}

	private HttpUriRequest createUriRequest(Request<?> request) {
		if (request.mMethod == Request.RequestMethod.GET) {
			if (request.getParams() != null) {
				String requestUrl = request.getUrl() + "?" + new String(request.getBody());
				return new HttpGet(requestUrl);
			}

			HttpGet getRequest = new HttpGet(request.getUrl());
			return getRequest;
		} else {
			HttpPost postRequest = new HttpPost(request.getUrl());
			if (request.getParams() != null) {
				List<NameValuePair> postParams = new ArrayList<NameValuePair>();
				for (String key : request.getParams().keySet()) {
					postParams.add(new BasicNameValuePair(key, request.getParams().get(key)));
				}
				UrlEncodedFormEntity formEntity = null;
				try {
					formEntity = new UrlEncodedFormEntity(postParams, request.getParamsEncoding());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				postRequest.setEntity(formEntity);
			}
			return postRequest;
		}
	}
}
