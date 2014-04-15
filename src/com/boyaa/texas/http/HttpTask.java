package com.boyaa.texas.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import android.util.Log;

/**
 * Http 请求任务
 * 
 * @author HuiLiu
 * 
 */
public class HttpTask implements Runnable {

	private static int DEFAULT_POOL_SIZE = 4096;
	private Request<?> request;
	private final HttpWorker httpWork;
	private ByteArrayPool mPool;
	private ResponsePoster mPoster;
	private static final int MAX_RETRY_COUNT = 2;

	public HttpTask(Request<?> request, ResponsePoster poster, HttpWorker worker) {
		this(request, poster, new ByteArrayPool(DEFAULT_POOL_SIZE), worker);
	}

	public HttpTask(Request<?> req, ResponsePoster poster, ByteArrayPool pool, HttpWorker worker) {
		request = req;
		mPool = pool;
		mPoster = poster;
		httpWork = worker;
	}

	@Override
	public void run() {
		int tryTimes = 0;
		Response<?> response = null;
		while (tryTimes < MAX_RETRY_COUNT) {
			tryTimes++;
			HttpResponse httpResponse = null;
			try {
				if (isInterrupted() || request.isCancled()) {
					return;
				}
				httpResponse = httpWork.doHttpRquest(request);
				StatusLine statusLine = httpResponse.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == HttpStatus.SC_OK) {
					if (httpResponse.getEntity() != null) {
						byte[] data = entityToBytes(httpResponse.getEntity());
						response = request.parseResponse(data);
						break;
					}
				} else {
					response = Response.error(new Error(statusCode, "not SC_OK error"));
				}
			} catch (IOException e) {
				if (Constants.DEBUG)
					e.printStackTrace();
				response = Response.error(new Error(Error.NETWORK_ERROR, "network error"));
			}
		}
		if (!request.isCancled()) {
			mPoster.dispatchResponse(request, response);
		}
	}

	private boolean isInterrupted() {
		return Thread.interrupted();
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
