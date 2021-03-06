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

package com.vimc.ahttp;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import com.vimc.ahttp.Request.ByteParameter;
import com.vimc.ahttp.Request.FileParameter;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

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
		setConnectionParametersByRequest(connection, request);
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
			Map<String, String> map = request.getHeaders();
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

		connection.setConnectTimeout(request.connectTimeout);
		connection.setReadTimeout(request.soTimeout);
		connection.setUseCaches(false);
		connection.setDoInput(true);
		// connection.setRequestProperty("Connection", "close");

		if ("https".equals(url.getProtocol())) {
			if (mSslSocketFactory != null) {
				((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
			} else {
				setDefaultSSLSocketFactory();
			}
		}

		return connection;
	}

	private void setDefaultSSLSocketFactory() {
		SSLContext sslContext = null;
		try {
			X509TrustManagerImpl mtm = new X509TrustManagerImpl();
			TrustManager[] tms = new TrustManager[] { mtm };

			// 初始化X509TrustManager中的SSLContext
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tms, new java.security.SecureRandom());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 为javax.net.ssl.HttpsURLConnection设置默认的SocketFactory和HostnameVerifier
		if (sslContext != null) {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		}
		X509HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
	}

	private class X509TrustManagerImpl implements X509TrustManager {
		@SuppressWarnings("unused")
		X509TrustManager myJSSEX509TrustManager;

		public X509TrustManagerImpl() throws Exception {
			KeyStore ks = KeyStore.getInstance("BKS");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(ks);
			TrustManager tms[] = tmf.getTrustManagers();
			for (int i = 0; i < tms.length; i++) {
				if (tms[i] instanceof X509TrustManager) {
					myJSSEX509TrustManager = (X509TrustManager) tms[i];
					return;
				}
			}
		}

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

	private void setConnectionParametersByRequest(HttpURLConnection connection, Request<?> request) throws IOException {
		switch (request.requestMethod) {
		case GET:
			connection.setRequestMethod("GET");
			addBodyIfExists(connection, request);
			break;
		case POST:
			connection.setRequestMethod("POST");
			addBodyIfExists(connection, request);
			break;
		case HEAD:
			connection.setRequestMethod("HEAD");
			addBodyIfExists(connection, request);
			break;
		default:
			throw new IllegalStateException("Unknown method type.");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addBodyIfExists(HttpURLConnection connection, Request request) throws IOException {
		if (request.containsMutilpartData()) {
			connection.setDoOutput(true);
			connection.addRequestProperty(HEADER_CONTENT_TYPE, request.getBodyContentType());
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			
			if (request.getStringParams().size() > 0) {
				writeStringFields(request.getStringParams(), out, request.getBoundray());
			}
			if (request.getFileParams().size() > 0) {
				writeFiles(request.getFileParams(), out, request.getBoundray());
			}
			if (request.getByteParams().size() > 0) {
				writeBytes(request.getByteParams(), out, request.getBoundray());
			}
			out.flush();
			out.close();
		} else {
			byte[] body = request.getStringBody();
			if (body != null) {
				connection.setDoOutput(true);
				connection.addRequestProperty(HEADER_CONTENT_TYPE, request.getBodyContentType());
				DataOutputStream out = new DataOutputStream(connection.getOutputStream());
				out.write(body);
				out.flush();
				out.close();
			}
		}
	}

	String TWO_HYPHENS = "--", LINE_END = "\r\n";
	
	/**
	 * write string parameters
	 */
	private void writeStringFields(Map<String, String> params, DataOutputStream output, String boundary) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String key : params.keySet()) {
			sb.append(TWO_HYPHENS + boundary + LINE_END);
			sb.append("Content-Disposition: form-data; name=\"" + key + "\"" + LINE_END);
			sb.append(LINE_END);
			sb.append(params.get(key) + LINE_END);
		}
		output.writeBytes(sb.toString());// 发送表单字段数据
	}

	/**
	 * write file parameters
	 */
	@SuppressWarnings("rawtypes")
	private void writeFiles(ArrayList<FileParameter> fileParams, DataOutputStream out, String boundary) throws IOException {
		for (FileParameter fileParameter : fileParams) {
			writeDataStart(out, fileParameter.paramName, fileParameter.fileName, boundary);

			InputStream is = new FileInputStream(fileParameter.file);
			byte[] bytes = new byte[1024];
			int len = 0;
			while ((len = is.read(bytes)) != -1) {
				out.write(bytes, 0, len);
			}
			is.close();
			
			writeDataEnd(out, boundary);
		}
	}
	
	/**
	 * write byte[] parameters
	 */
	@SuppressWarnings("rawtypes")
	private void writeBytes(ArrayList<ByteParameter> byteParams, DataOutputStream out, String boundary) throws IOException {
		for (ByteParameter byteParameter : byteParams) {
			writeDataStart(out, byteParameter.paramName, byteParameter.fileName, boundary);
			out.write(byteParameter.data);
			writeDataEnd(out, boundary);
		}
	}

	private void writeDataStart(DataOutputStream out, String paramaName, String fileName, String boundary) throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append(TWO_HYPHENS);
		sb.append(boundary);
		sb.append(LINE_END);
		sb.append("Content-Disposition: form-data; name=\"" + paramaName + "\"; filename=\"" + fileName + "\"" + LINE_END);
		sb.append("Content-Type: application/octet-stream; charset=" + "utf-8" + LINE_END);
		sb.append(LINE_END);
		out.write(sb.toString().getBytes());
	}
	
	private void writeDataEnd(DataOutputStream out, String boundary) throws IOException {
		out.write(LINE_END.getBytes());
		byte[] end_data = (TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_END).getBytes();
		out.write(end_data);
	}
}
