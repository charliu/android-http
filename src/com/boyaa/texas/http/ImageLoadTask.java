package com.boyaa.texas.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

/**
 * 图片下载任务
 * 
 * @author CharLiu
 * 
 */
public class ImageLoadTask implements Runnable {
	final ImageLoaderEngine engine;
	final ImageLoadingInfo loadingInfo;
	final HttpWorker httpWorker;
	private final Options defaultDecodeOptions = ImageLoader.getDefaultOptions();

	public ImageLoadTask(ImageLoadingInfo info, ImageLoaderEngine engine, HttpWorker worker) {
		this.loadingInfo = info;
		this.engine = engine;
		this.httpWorker = worker;
	}

	@Override
	public void run() {
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			//
//		}
		Bitmap bmp = engine.getFromMemoryCache(loadingInfo.cacheKey);
		if (bmp != null && !bmp.isRecycled()) {
			engine.post(new DispalyTask(loadingInfo, bmp));
			engine.cancelDisplayTaskFor(loadingInfo.imageWrapper);
			return;
		}
		if (taskNotActual()) {
			Log.e(Constants.HTTP_TAG, "Loading task canceled");
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

			if (taskNotActual()) {
				Log.e(Constants.HTTP_TAG, "display reused, cancel!!!");
				return;
			}
			engine.cancelDisplayTaskFor(info.imageWrapper);
			if (bitmap != null) {
				info.listener.onSuccess(info.uri, info.imageWrapper.getImageView(), bitmap);
			} else {
				info.listener.onError(new Error(Error.UNKNOWN_ERROR));
			}

		}

	}

	private Bitmap loadBitmap() throws IOException {
		Bitmap bitmap = engine.getFromDiskCache(loadingInfo.cacheKey);
		if (bitmap != null) {
			engine.putToMemoryCache(loadingInfo.cacheKey, bitmap);
			if (Constants.DEBUG) {
				Log.i(Constants.HTTP_TAG, "Load image from disk cache");
			}
			engine.putToMemoryCache(loadingInfo.cacheKey, bitmap);
			return bitmap;
		}
		if (Constants.DEBUG) {
			Log.i(Constants.HTTP_TAG, "Start download image from internet");
		}
		Request<Bitmap> request = new BitmapRequest(loadingInfo.uri);
		HttpResponse httpResponse = httpWorker.doHttpRquest(request);
		StatusLine statusLine = httpResponse.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
			HttpEntity entity = httpResponse.getEntity();
			try {
				InputStream in = entity.getContent();
				if (in == null) {
					throw new IOException("Bitmap inputstream is null");
				}
				bitmap = BitmapFactory.decodeStream(in, null, defaultDecodeOptions);
			} finally {
				try {
					entity.consumeContent();
				} catch (Exception e) {
					// Do nothing
				}
			}
			if (bitmap != null) {
				engine.putToMemoryCache(loadingInfo.cacheKey, bitmap);
				engine.putToDiskCache(loadingInfo.cacheKey, bitmap);
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
