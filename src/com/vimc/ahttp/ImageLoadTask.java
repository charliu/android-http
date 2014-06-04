package com.vimc.ahttp;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import com.vimc.ahttp.ImageLoadListener.LoadFrom;
import com.vimc.ahttp.Request.RequestMethod;

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
	private LoadFrom loadFrom;
	
	public ImageLoadTask(ImageLoadingInfo info, ImageLoaderEngine engine, HttpWorker worker) {
		this.loadingInfo = info;
		this.engine = engine;
		this.httpWorker = worker;
	}

	@Override
	public void run() {
		ReentrantLock loadLock = loadingInfo.mLock;
		if (loadLock.isLocked()) {
			HLog.w("LoadLock locked URL:" + loadingInfo.uri);
		}
		loadLock.lock();
		
		Bitmap bitmap = null;
		try {
			if (taskNotActual("BeforeLoad"))
				return;
			bitmap = engine.getFromMemoryCache(loadingInfo.cacheKey);
			if (bitmap != null && !bitmap.isRecycled()) {
				loadFrom = LoadFrom.FROM_MEMORY;
				engine.post(new DispalyTask(loadingInfo, bitmap));
				return;
			}
			bitmap = loadBitmap();
			if (taskNotActual("AfterLoad"))
				return;
		} catch (Exception e) {
			if (HLog.Config.LOG_E)
				e.printStackTrace();
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
				info.listener.onSuccess(info.uri, info.imageWrapper.getImageView(), bitmap, loadFrom);
			} else {
				info.listener.onError(new HError(HError.UNKNOWN_ERROR));
			}

		}

	}

	private Bitmap loadBitmap() throws IOException {
		Bitmap bitmap = engine.getFromDiskCache(loadingInfo.cacheKey);
		if (bitmap != null) {
			loadFrom = LoadFrom.FORM_SDCARD;
			engine.putToMemoryCache(loadingInfo.cacheKey, bitmap);
			HLog.i("Load image from disk cache, URL:" + loadingInfo.uri);
			return bitmap;
		}
		HLog.i("Starting download, URL:" + loadingInfo.uri);
		loadFrom = LoadFrom.FROM_INTERNET;
		Request<Bitmap> request = new BitmapRequest(loadingInfo.uri);
		request.requestMethod = RequestMethod.GET;
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
				try {
					bitmap = BitmapFactory.decodeStream(in, null, defaultDecodeOptions);
				} catch(OutOfMemoryError error) {
					if (HLog.Config.LOG_E) error.printStackTrace();
				}
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
