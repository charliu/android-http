package com.vim.ahttp;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * Image加载类，单例 {@link #load(String, ImageView) #load(String, ImageView, ImageLoadListener)}
 * 
 * @author CharLiu
 */
public class ImageLoader {

	private ImageLoaderEngine engine;

	private final HttpWorker mHttpWorker;

	private volatile static ImageLoader instance;

	private static String DEFAULT_DISK_CACHE_DIR = "texas" + File.separator + ".Cache";

	public static ImageLoader getInstance() {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new ImageLoader(new ImageLruCache(), new ImageDiskCache(getDefaultDiskCacheDir()));
				}
			}
		}
		return instance;
	}

	private static File getDefaultDiskCacheDir() {
		String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(sdCard + File.separator + DEFAULT_DISK_CACHE_DIR);
		if (!file.exists()) {
			if (file.mkdirs()) {
				return file;
			} else {
				HLog.e("create disk cache dir fail");
			}
		}
		return file;
	}

	private ImageLoader(Cache<Bitmap> memoryCache, Cache<Bitmap> diskCache) {
		engine = new ImageLoaderEngine(memoryCache, diskCache);
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

	public void load(final String imageUrl, ImageView view, final ImageLoadListener listener) {
		load(imageUrl, new ImageViewWrapper(view), listener);
	}

	private void load(final String url, final ImageViewWrapper imageWrapper, final ImageLoadListener listener) {
		throwIfNotInMainThread();

		final String cacheKey = MD5Util.generateMD5(url);

		if (TextUtils.isEmpty(url))
			return;
		Bitmap cachedBitmap = engine.getFromMemoryCache(cacheKey);
		if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
			HLog.d("Load Image from memory cache");
			listener.onSuccess(url, imageWrapper.getImageView(), cachedBitmap);
			return;
		}
		listener.onSuccess(url, imageWrapper.imageViewRef.get(), null);
		engine.prepareDisplayTaskFor(imageWrapper, cacheKey);

		ImageLoadingInfo loadingInfo = new ImageLoadingInfo(url, imageWrapper, listener, cacheKey,
				engine.getLockForUri(url));
		ImageLoadTask loadingTask = new ImageLoadTask(loadingInfo, engine, mHttpWorker);
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
	 * 返回默认的ImageLoadListener
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

	/**
	 * 默认decode options
	 * 
	 * @return
	 */
	public static Options getDefaultOptions() {
		Options options = new Options();
		options.inPreferredConfig = Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		return options;
	}
}
