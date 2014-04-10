/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.boyaa.texas.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import com.boyaa.texas.http.Request.RequestMethod;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * An {@link HttpStack} based on {@link HttpURLConnection}.
 */
public class HurlWorker implements HttpWorker {

	private static final String HEADER_CONTENT_TYPE = "Content-Type";

	/**
	 * An interface for transforming URLs before use.
	 */
	public interface UrlRewriter {
		/**
		 * Returns a URL to use instead of the provided one, or null to indicate
		 * this URL should not be used at all.
		 */
		public String rewriteUrl(String originalUrl);
	}

	private final UrlRewriter mUrlRewriter;
	private final SSLSocketFactory mSslSocketFactory;

	public HurlWorker() {
		this(null);
	}

	/**
	 * @param urlRewriter
	 *            Rewriter to use for request URLs
	 */
	public HurlWorker(UrlRewriter urlRewriter) {
		this(urlRewriter, null);
	}

	/**
	 * @param urlRewriter
	 *            Rewriter to use for request URLs
	 * @param sslSocketFactory
	 *            SSL factory to use for HTTPS connections
	 */
	public HurlWorker(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
		mUrlRewriter = urlRewriter;
		mSslSocketFactory = sslSocketFactory;
	}

	@Override
	public HttpResponse doHttpRquest(Request<?> request) throws IOException {
		String url = request.getUrl();

		if (mUrlRewriter != null) {
			String rewritten = mUrlRewriter.rewriteUrl(url);
			if (rewritten == null) {
				throw new IOException("URL blocked by rewriter: " + url);
			}
			url = rewritten;
		}
		URL parsedUrl = new URL(url);
		HttpURLConnection connection = openConnection(parsedUrl, request);
		addHeadersIfExists(request, connection);
		setConnectionParametersForRequest(connection, request);
		// Initialize HttpResponse with data from the HttpURLConnection.
		ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
		int responseCode = connection.getResponseCode();
		if (responseCode == -1) {
			// -1 is returned by getResponseCode() if the response code could
			// not be retrieved.
			// Signal to the caller that something was wrong with the
			// connection.
			throw new IOException("Could not retrieve response code from HttpUrlConnection.");
		}
		StatusLine responseStatus = new BasicStatusLine(protocolVersion, connection.getResponseCode(),
				connection.getResponseMessage());
		BasicHttpResponse response = new BasicHttpResponse(responseStatus);
		response.setEntity(entityFromConnection(connection));
		for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
			if (header.getKey() != null) {
				Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
				response.addHeader(h);
			}
		}
		return response;
	}
	
	private void addHeadersIfExists(Request<?> request, HttpURLConnection conn) {
		if (request.getHeaders() != null && request.getHeaders().size() > 0) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.putAll(request.getHeaders());
			for (String headerName : map.keySet()) {
				conn.addRequestProperty(headerName, map.get(headerName));
			}
		}
	}

	/**
	 * Initializes an {@link HttpEntity} from the given
	 * {@link HttpURLConnection}.
	 * 
	 * @param connection
	 * @return an HttpEntity populated with data from <code>connection</code>.
	 */
	private static HttpEntity entityFromConnection(HttpURLConnection connection) {
		BasicHttpEntity entity = new BasicHttpEntity();
		InputStream inputStream;
		try {
			inputStream = connection.getInputStream();
		} catch (IOException ioe) {
			inputStream = connection.getErrorStream();
		}
		entity.setContent(inputStream);
		entity.setContentLength(connection.getContentLength());
		entity.setContentEncoding(connection.getContentEncoding());
		entity.setContentType(connection.getContentType());
		return entity;
	}

	/**
	 * Create an {@link HttpURLConnection} for the specified {@code url}.
	 */
	protected HttpURLConnection createConnection(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}

	/**
	 * Opens an {@link HttpURLConnection} with parameters.
	 * 
	 * @param url
	 * @return an open connection
	 * @throws IOException
	 */
	private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
		HttpURLConnection connection = createConnection(url);

		int timeoutMs = request.getSocketTimeout();
		connection.setConnectTimeout(timeoutMs);
		connection.setReadTimeout(timeoutMs);
		connection.setUseCaches(false);
		connection.setDoInput(true);

		// use caller-provided custom SslSocketFactory, if any, for HTTPS
		if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
			((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
		}

		return connection;
	}

	static void setConnectionParametersForRequest(HttpURLConnection connection, Request<?> request) throws IOException {
		switch (request.getMethod()) {
		case RequestMethod.GET:
			connection.setRequestMethod("GET");
			break;
		case RequestMethod.POST:
			connection.setRequestMethod("POST");
			addBodyIfExists(connection, request);
			break;
		default:
			throw new IllegalStateException("Unknown method type.");
		}
	}

	private static void addBodyIfExists(HttpURLConnection connection, Request<?> request) throws IOException {
		byte[] body = request.getBody();
		if (body != null) {
			connection.setDoOutput(true);
			connection.addRequestProperty(HEADER_CONTENT_TYPE, request.getBodyContentType());
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.write(body);
			out.close();
		}
	}
}
