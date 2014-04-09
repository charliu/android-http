package com.boyaa.texas.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import android.net.http.AndroidHttpClient;
import android.util.Log;

public class HttpTask implements Runnable {

	private static int DEFAULT_POOL_SIZE = 4096;
	private Request<?> request;
	private final HttpWorker httpWork;
	private ByteArrayPool mPool;
	private ResponsePoster mPoster;

	public HttpTask(Request<?> request, ResponsePoster poster, HttpWorker worker) {
		this(request, poster, new ByteArrayPool(DEFAULT_POOL_SIZE), worker);
	}

	public HttpTask(Request<?> request, ResponsePoster poster) {
		this(request, poster, new ByteArrayPool(DEFAULT_POOL_SIZE), new HttpClientWorker(
				AndroidHttpClient.newInstance("volley/0")));
	}
	

	public HttpTask(Request<?> req, ResponsePoster poster, ByteArrayPool pool, HttpWorker worker) {
		request = req;
		mPool = pool;
		mPoster = poster;
		httpWork = worker;
	}
	
	private HttpWorker getHttpWorkerBySDK() {
		//TODO
		return null;
	}

	@Override
	public void run() {
		HttpResponse httpResponse = null;
		Response<?> response = null;
		try {
			httpResponse = httpWork.doHttpRquest(request);
			StatusLine statusLine = httpResponse.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				if (httpResponse.getEntity() != null) {
					byte[] data = entityToBytes(httpResponse.getEntity());
					response = request.parseResponse(data);
				}
			} else {
				response = Response.error(new Error(statusCode, ""));
			}
		} catch (IOException e) {
			e.printStackTrace();
			response = Response.error(new Error(Error.NETWORK_ERROR, "network error"));
		} finally {
			if (response == null) {
				response = Response.error(new Error(Error.UNKNOWN_ERROR, "unknown error"));
			}
		}
		mPoster.dispatchResponse(request, response);
	}

	private byte[] entityToBytes(HttpEntity entity) throws IOException {
		PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, (int) entity.getContentLength());
		byte[] buffer = null;
		try {
			InputStream in = entity.getContent();
			if (in == null) {
				throw new IOException();
			}
			buffer = mPool.getBuf(1024);
			int count;
			while ((count = in.read(buffer)) != -1) {
				bytes.write(buffer, 0, count);
			}
			return bytes.toByteArray();
		} finally {
			try {
				// Close the InputStream and release the resources by
				// "consuming the content".
				entity.consumeContent();
			} catch (IOException e) {
				// This can happen if there was an exception above that left the
				// entity in
				// an invalid state.
				Log.e("HTTP", "Error occured when calling consumingContent");
			}
			mPool.returnBuf(buffer);
			bytes.close();
		}
	}

}
