package com.vimc.ahttp;

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
		Bitmap bmp = engine.getFromMemoryCache(loadingInfo.cacheKey);
		if (bmp != null && !bmp.isRecycled()) {
			engine.post(new DispalyTask(loadingInfo, bmp));
			engine.cancelDisplayTaskFor(loadingInfo.imageWrapper);
			return;
		}
		if (taskNotActual("BeforeLoad")) {
			return;
		}
		ReentrantLock loadLock = loadingInfo.mLock;
		loadLock.lock();
		Bitmap bitmap = null;
		try {
			bitmap = loadBitmap();
			if (taskNotActual("AfterLoad"))
				return;
		} catch (Exception e) {
			HLog.e(e.getMessage());
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

			if (taskNotActual("DisplayTask")) {
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
			HLog.i("Load image from disk cache in SubThread, URL:" + loadingInfo.uri);
			return bitmap;
		}
		HLog.i("Starting download, URL:" + loadingInfo.uri);
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
			if (bitmap == null) {
				HLog.e("Image can't be decode, URL:" + loadingInfo.uri);
			} else {
				engine.putToMemoryCache(loadingInfo.cacheKey, bitmap);
				engine.putToDiskCache(loadingInfo.cacheKey, bitmap);
				return bitmap;
			}
		}
		return null;
	}

	private boolean taskNotActual(String logPrefix) {
		if (loadingInfo.imageWrapper.isCollected()) {
			HLog.w(logPrefix + " ImageView is collected");
			return true;
		}
		if (engine.isReused(loadingInfo.imageWrapper, loadingInfo.cacheKey)) {
			HLog.w(logPrefix + " ImageView is reused");
			return true;
		}
		return false;
	}

}
