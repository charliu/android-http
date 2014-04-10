package com.boyaa.texas.http;

import java.util.HashMap;
import java.util.Map;

import com.boyaa.texas.http.Response.ResponseHandler;

import android.graphics.Bitmap;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {

	private ImageCache imageCache;
	private Map<String, BitmapRequest> currentRequest = new HashMap<String, BitmapRequest>();
	
	public ImageLoader(ImageCache cache) {
		this.imageCache = cache;
	}
	
	public interface ImageCache {
		void put(String key, Bitmap bitmap);

		Bitmap get(String key);
	}

	public interface ImageHandler {
		void onSuccess(Bitmap bitmap);

		void onError(Error error);
	}
	
	public void load(final String imageUrl, ImageView view) {
		load(imageUrl, ImageLoader.getImageHandler(view, 0, 0));
	}

	public void load(final String imageUrl, final ImageHandler imageHandler) {
		checkCallInMainThread();
		if (TextUtils.isEmpty(imageUrl))
			return;
		Bitmap bitmap = getFromCache(imageUrl);
		if (bitmap != null) {
			imageHandler.onSuccess(bitmap);
			Log.d("Cache", "Load Image from cache");
			return;
		}
		if (currentRequest.get(imageUrl) != null) {
			Log.d("Cache", "Request already in map");
			return;
		}
		imageHandler.onSuccess(null); //set default image
		BitmapRequest request = new BitmapRequest(imageUrl, new ResponseHandler<Bitmap>() {
			@Override
			public void onSuccess(Bitmap response) {
				imageHandler.onSuccess(response);
				putToCache(imageUrl, response);
				currentRequest.remove(imageUrl);
			}
			@Override
			public void onError(Error error) {
				imageHandler.onError(error);
				currentRequest.remove(imageUrl);
			}
		});
		currentRequest.put(imageUrl, request);
		RequestExecutor.execute(request);
	}
	
	
	/**
	 * 判断当前是否为主线程调用
	 */
	private void checkCallInMainThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be call from the main thread.");
        }
	}
	
	private void putToCache(String key, Bitmap bitmap) {
		if (imageCache != null) {
			imageCache.put(key, bitmap);
		}
	}

	private Bitmap getFromCache(String key) {
		if (imageCache == null)
			return null;
		return imageCache.get(key);
	}

	/**
	 * 返回默认的ImageHandler
	 * @param view 
	 * @param defaultImageResId 默认图片
	 * @param errorImageResId 下载失败后的图片
	 * @return
	 */
	public static ImageHandler getImageHandler(final ImageView view, final int defaultImageResId,
			final int errorImageResId) {
		return new ImageHandler() {
			@Override
			public void onError(Error error) {
				if (errorImageResId != 0) {
					view.setImageResource(errorImageResId);
				}
			}
			@Override
			public void onSuccess(Bitmap response) {
				if (response != null) {
					view.setImageBitmap(response);
				} else if (defaultImageResId != 0) {
					view.setImageResource(defaultImageResId);
				}
			}
		};
	}
}
