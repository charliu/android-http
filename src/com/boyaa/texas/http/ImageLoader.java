package com.boyaa.texas.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.boyaa.texas.http.Response.ResponseHandler;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {

	private Cache<Bitmap> imageCache;

	@SuppressLint("UseSparseArrays")
	private final Map<Integer, String> cacheKeysForImageViewWrapper = Collections
			.synchronizedMap(new HashMap<Integer, String>());

	// private final Map<String, ReentrantLock> uriLocks = new
	// WeakHashMap<String, ReentrantLock>();

	private final HashMap<String, BitmapRequest> mInFlightRequests = new HashMap<String, BitmapRequest>();

	
	private final static HttpWorker mWorker;
	static {
		if (Build.VERSION.SDK_INT >= 9) {
			mWorker = new HurlWorker();
		} else {
			// Prior to Gingerbread, HttpUrlConnection was unreliable.
			// See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
			mWorker = new HttpClientWorker(AndroidHttpClient.newInstance("android"));
		}
	}
	
	public ImageLoader(Cache<Bitmap> cache) {
		this.imageCache = cache;
	}

	public interface ImageLoadListener {
		void onSuccess(String imageUrl, ImageView imageView, Bitmap bitmap);

		void onError(Error error);
	}

	public void load(final String imageUrl, ImageView view) {
		load(imageUrl, view, 0, 0);
	}

	public void load(final String imageUrl, ImageView view, int defaultImage) {
		load(imageUrl, view, defaultImage, 0);
	}

	public void load(final String imageUrl, ImageView view, int defaultImage, int errorImage) {
		load(imageUrl, new ImageViewWrapper(view),
				ImageLoader.getImageLoadListener(imageUrl, view, defaultImage, errorImage));
	}

	int id = 0;
	
	private ImageLoaderEngine engine = new ImageLoaderEngine(new ImageLruCache());
	
	public void load(final String url, final ImageViewWrapper imageWrapper, final ImageLoadListener listener) {
		throwIfNotInMainThread();

		final String cacheKey = CacheKeyUtil.generate(url);

		if (TextUtils.isEmpty(url))
			return;
		Bitmap cachedBitmap = engine.getFromCache(cacheKey);
		if (cachedBitmap != null) {
			Log.d("Cache", "Load Image from cache");
			listener.onSuccess(url, imageWrapper.getImageView(), cachedBitmap);
			mInFlightRequests.remove(cacheKey);
			return;
		}
		imageWrapper.imageViewRef.get().setImageResource(R.drawable.ic_launcher);
		engine.prepareDisplayTaskFor(imageWrapper, cacheKey);

		ImageLoadingInfo loadingInfo = new ImageLoadingInfo(url, imageWrapper, listener, cacheKey, engine.getLockForUri(url));
		LoadImageTask loadingTask = new LoadImageTask(loadingInfo, engine, mWorker);
		engine.submit(loadingTask);
//		final int index = id + 1;
//		id += 1;
//		BitmapRequest request = new BitmapRequest(url, new ResponseHandler<Bitmap>() {
//
//			@Override
//			public void onSuccess(Bitmap bitmap) {
//				putToCache(cacheKey, bitmap);
//				mInFlightRequests.remove(cacheKey);
//
//				if (imageWrapper.isCollected()) {
//					Log.e("HTTP", "collected at:" + index);
//				} else if (isReused(imageWrapper, cacheKey)) {
//					Log.e("HTTP", "reused at:" + index);
//				} else {
//					putToCache(cacheKey, bitmap);
//					listener.onSuccess(url, imageWrapper.getImageView(), bitmap);
//					cancelDisplayTaskFor(imageWrapper);
//				}
//			}
//
//			@Override
//			public void onError(Error error) {
//				mInFlightRequests.remove(cacheKey);
//				cancelDisplayTaskFor(imageWrapper);
//				listener.onError(error);
//			}
//		});
	}
	
	
	/**
	 * 
	 * @param wrapper
	 * @param memoryCacheKey
	 */
	void prepareDisplayTaskFor(ImageViewWrapper wrapper, String memoryCacheKey) {
		cacheKeysForImageViewWrapper.put(wrapper.getId(), memoryCacheKey);
	}

	/**
	 * 
	 */
	void cancelDisplayTaskFor(ImageViewWrapper wrapper) {
		cacheKeysForImageViewWrapper.remove(wrapper.getId());
	}

	String getLoadingUriForView(ImageViewWrapper wrapper) {
		return cacheKeysForImageViewWrapper.get(wrapper.getId());
	}
	
	/**
	 * 判断view是否重用，如果是重用的view就不更新ui，防止使用viewholder错乱
	 * @param memoryCacheKey
	 * @return
	 */
	private boolean isReused(ImageViewWrapper wrapper, String cacheKey) {
		String currentCacheKey = getLoadingUriForView(wrapper);
		return !cacheKey.equals(currentCacheKey);
	}

	/**
	 * 判断当前是否为主线程调用
	 */
	private void throwIfNotInMainThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("ImageLoader must be call from the main thread.");
		}
	}

	private void putToCache(String cacheKey, Bitmap bitmap) {
		if (imageCache != null) {
			imageCache.put(cacheKey, bitmap);
		}
	}

	private Bitmap getFromCache(String cacheKey) {
		if (imageCache == null)
			return null;
		return imageCache.get(cacheKey);
	}

	/**
	 * 返回默认的ImageHandler
	 * 
	 * @param view
	 * @param defaultImageResId
	 *            默认图片
	 * @param errorImageResId
	 *            下载失败后的图片
	 * @return
	 */
	public static ImageLoadListener getImageLoadListener(final String url, final ImageView view,
			final int defaultImageResId, final int errorImageResId) {
		return new ImageLoadListener() {
			@Override
			public void onError(Error error) {
				if (errorImageResId != 0) {
					view.setImageResource(errorImageResId);
				}
			}

			@Override
			public void onSuccess(String url, ImageView view, Bitmap bitmap) {
				if (bitmap != null) {
					view.setImageBitmap(bitmap);
				} else if (defaultImageResId != 0) {
					view.setImageResource(defaultImageResId);
				}
			}
		};
	}
}
