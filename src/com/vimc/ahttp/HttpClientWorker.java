package com.vimc.ahttp;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
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
		switch(request.mMethod) {
		
		case Request.RequestMethod.GET:
			return new HttpGet(request.getRequestUrl());
		case Request.RequestMethod.POST:
			HttpPost postRequest = new HttpPost(request.getUrl());
			HttpEntity entity = request.getPostEntity();
			if (entity != null) {
				postRequest.setEntity(entity);
			}
			return postRequest;
		case Request.RequestMethod.HEAD:
			return new HttpHead(request.getRequestUrl());
			default:
				throw new IllegalArgumentException("Http request method not support");
		}
	}
}
