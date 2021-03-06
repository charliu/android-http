package com.vimc.ahttp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
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
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.vimc.ahttp.Request.ByteParameter;
import com.vimc.ahttp.Request.FileParameter;

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
		// httpRequest.setHeader("Connection", "close");
		addRequestHeader(httpRequest, request.getHeaders());
		HttpParams httpParams = httpRequest.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, request.connectTimeout);
		HttpConnectionParams.setSoTimeout(httpParams, request.soTimeout);

		return mClient.execute(httpRequest);
	}

	private void addRequestHeader(HttpUriRequest request, Map<String, String> headers) {
		if (headers != null) {
			for (String key : headers.keySet()) {
				request.addHeader(key, headers.get(key));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private HttpUriRequest createUriRequest(Request<?> request) throws UnsupportedEncodingException {
		switch (request.requestMethod) {

		case GET:
			return new HttpGet(request.getFullGetRequestUrl());
		case POST:
			HttpPost postRequest = new HttpPost(request.getUrl());
			if (!request.containsMutilpartData()) {
				postRequest.setEntity(request.getStringHttpEntity());
			} else {
				MultipartEntity entity = new MultipartEntity();
				Map<String, String> stringParams = request.getStringParams();
				for (String key : stringParams.keySet()) {
					entity.addPart(key, new StringBody(stringParams.get(key), Charset.forName(request.getParamsEncoding())));
				}
				for (FileParameter fileParameter : request.getFileParams()) {
					entity.addPart(fileParameter.paramName, new FileBody(fileParameter.file, 
							fileParameter.fileName, "multipart/form-data", request.getParamsEncoding()));
				}
				for (ByteParameter byteParameter : request.getByteParams()) {
					entity.addPart(byteParameter.paramName, new ByteArrayBody(byteParameter.data, byteParameter.fileName));
				}
				postRequest.setEntity(entity);
			}

			return postRequest;
		case HEAD:
			return new HttpHead(request.getFullGetRequestUrl());
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
