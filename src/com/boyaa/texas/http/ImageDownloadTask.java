package com.boyaa.texas.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

/**
 * 图片下载任务
 * 
 * @author CharLiu
 * 
 */
public class ImageDownloadTask implements Runnable {
	final ImageLoaderEngine engine;
	final ImageLoadingInfo loadingInfo;
	final HttpWorker httpWorker;

	public ImageDownloadTask(ImageLoadingInfo info, ImageLoaderEngine engine, HttpWorker worker) {
		this.loadingInfo = info;
		this.engine = engine;
		this.httpWorker = worker;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			//
		}
		Bitmap bmp = engine.getFromCache(loadingInfo.cacheKey);
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
			bitmap = downloadBitmap();
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

	private final Options defaultDecodeOptions = createDefaultOptions();

	private Options createDefaultOptions() {
		Options options = new Options();
		options.inPreferredConfig = Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		return options;
	}

	private Bitmap downloadBitmap() throws IOException {
		if (Constants.DEBUG) {
			Log.i(Constants.HTTP_TAG, "Start download image from internet");
		}
		Request<Bitmap> request = new BitmapRequest(loadingInfo.uri);
		HttpResponse httpResponse = httpWorker.doHttpRquest(request);
		StatusLine statusLine = httpResponse.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
			HttpEntity entity = httpResponse.getEntity();
			Bitmap bitmap = null;
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
