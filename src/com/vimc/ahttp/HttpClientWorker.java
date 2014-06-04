package com.vimc.ahttp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * Use HttpClient execute an request, return HttpReqponse
 * 
 * @author CharLiu
 * 
 */
public class HttpClientWorker implements HttpWorker {
	private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;
	protected final HttpClient mClient;

	public HttpClientWorker(HttpClient httpClient) {
		if (httpClient != null) {
			this.mClient = httpClient;
		} else {
			this.mClient = newHttpClient();
		}
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
		switch (request.requestMethod) {

		case GET:
			return new HttpGet(request.getRequestUrl());
		case POST:
			HttpPost postRequest = new HttpPost(request.getUrl());
			HttpEntity entity = request.getPostEntity();
			if (entity != null) {
				postRequest.setEntity(entity);
			}
			return postRequest;
		case HEAD:
			return new HttpHead(request.getRequestUrl());
		default:
			throw new IllegalArgumentException("Http request method not support");
		}
	}

	private HttpClient newHttpClient() {
		HttpParams params = new BasicHttpParams();

		HttpConnectionParams.setStaleCheckingEnabled(params, false);

		HttpConnectionParams.setConnectionTimeout(params, SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpClientParams.setRedirecting(params, false);
		HttpProtocolParams.setUserAgent(params, "Android");
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			SSLSocketFactoryImpl sslSocketFactory = new SSLSocketFactoryImpl(trustStore);
			sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		} catch (Exception e) {
			HLog.printThrowable(e);
		}

		ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(manager, params);
	}

	private class SSLSocketFactoryImpl extends SSLSocketFactory {

		SSLContext sslContext = SSLContext.getInstance("TLS");

		public SSLSocketFactoryImpl(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws java.security.cert.CertificateException {

				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws java.security.cert.CertificateException {

				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
				UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
