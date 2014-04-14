package com.boyaa.texas.http;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class LoadImageTask implements Runnable {
	final ImageLoaderEngine engine;
	final ImageLoadingInfo loadingInfo;
	final HttpWorker httpWorker;

	public LoadImageTask(ImageLoadingInfo info, ImageLoaderEngine engine, HttpWorker worker) {
		this.loadingInfo = info;
		this.engine = engine;
		this.httpWorker = worker;
	}

	@Override
	public void run() {
		if (taskNotActual()) {
			return;
		}
		ReentrantLock loadLock = loadingInfo.mLock;
		loadLock.lock();
		Bitmap bitmap = null;
		try {
			bitmap = loadBitmap();
			if (taskNotActual())
				return;
		} catch (Exception e) {
			bitmap = null;
		} finally {
			loadLock.unlock();
		}
		engine.post(new DispalyTask(loadingInfo, bitmap));
	}

	private class DispalyTask implements Runnable {
		ImageLoadingInfo info;
		Bitmap bitmap;

		public DispalyTask(ImageLoadingInfo info, Bitmap bitmap) {
			this.info = info;
			this.bitmap = bitmap;
		}

		@Override
		public void run() {
			if (taskNotActual())
				return;
			if (bitmap != null) {
				info.listener.onSuccess(info.uri, info.imageWrapper.getImageView(), bitmap);
			} else {
				info.listener.onError(new Error(Error.UNKNOWN_ERROR));
			}

		}

	}

	private Bitmap loadBitmap() throws IOException {
		Request<Bitmap> request = new BitmapRequest(loadingInfo.uri);
		HttpResponse httpResponse = httpWorker.doHttpRquest(request);
		StatusLine statusLine = httpResponse.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
			HttpEntity entity = httpResponse.getEntity();
			byte[] data = EntityUtils.toByteArray(entity);
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			if (bitmap != null) {
				engine.putToCache(loadingInfo.cacheKey, bitmap);
				return bitmap;
			}
		}
		return null;
	}

	private boolean taskNotActual() {
		return loadingInfo.imageWrapper.isCollected()
				|| engine.isReused(loadingInfo.imageWrapper, loadingInfo.cacheKey);
	}

}
