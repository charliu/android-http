package com.boyaa.texas.http;

import android.graphics.Bitmap;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

/**
 * 
 * @author CharLiu
 * 
 */
public class ImageLoader {

	private ImageLoaderEngine engine;

	private final HttpWorker mHttpWorker;

	private static ImageLoader instance;

	public static ImageLoader getInstance() {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new ImageLoader(new ImageLruCache());
				}
			}
		}
		return instance;
	}

	private ImageLoader(Cache<Bitmap> cache) {
		engine = new ImageLoaderEngine(cache);
		mHttpWorker = HttpWorkerFactory.createHttpWorker();
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

	public void load(final String url, final ImageViewWrapper imageWrapper, final ImageLoadListener listener) {
		throwIfNotInMainThread();

		final String cacheKey = CacheKeyUtil.generate(url);

		if (TextUtils.isEmpty(url))
			return;
		Bitmap cachedBitmap = engine.getFromCache(cacheKey);
		if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
			if (Constants.DEBUG) {
				Log.d(Constants.HTTP_TAG, "Load Image from cache");
			}
			listener.onSuccess(url, imageWrapper.getImageView(), cachedBitmap);
			return;
		}
		listener.onSuccess(url, imageWrapper.imageViewRef.get(), null);
		engine.prepareDisplayTaskFor(imageWrapper, cacheKey);

		ImageLoadingInfo loadingInfo = new ImageLoadingInfo(url, imageWrapper, listener, cacheKey,
				engine.getLockForUri(url));
		ImageDownloadTask loadingTask = new ImageDownloadTask(loadingInfo, engine, mHttpWorker);
		engine.submit(loadingTask);
	}

	/**
	 * 判断当前是否为主线程调用
	 */
	private void throwIfNotInMainThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("ImageLoader must be call from the main thread.");
		}
	}

	public static ImageLoadListener getImageLoadListener(final String url, final ImageView view) {
		return getImageLoadListener(url, view, 0, 0);
	}

	public static ImageLoadListener getImageLoadListener(final String url, final ImageView view,
			final int defaultImageResId) {
		return getImageLoadListener(url, view, defaultImageResId, 0);
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
