package com.vimc.ahttp;

import java.io.File;

import com.vimc.ahttp.ImageLoadListener.LoadFrom;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

/**
 * Image加载类，单例 {@link #load(String, ImageView) #load(String, ImageView,
 * ImageLoadListener)}
 * 
 * @author CharLiu
 */
public class ImageLoader {

	private ImageLoaderEngine engine;

	private HttpWorker mHttpWorker;

	private volatile static ImageLoader instance;

	private static String DEFAULT_DISK_CACHE_DIR = "texas" + File.separator + ".Cache";
	
	//默认图片内存缓存size为heapsize的1/8
	private static final int DEFAULT_MEMORY_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / (1024 * 8)); 

	private Cache<Bitmap> memoryCache;
	private Cache<Bitmap> diskCache;
	private ImageLoaderConfig loadConfig = null;
	private boolean inited = false;

	private ImageLoader() {
	}

	public static ImageLoader getInstance() {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new ImageLoader();
				}
			}
		}
		return instance;
	}

	public void init(ImageLoaderConfig config) {
		if (TextUtils.isEmpty(config.diskCacheDir) || config.decodeOptions == null || config.memoryCacheSize < 0) {
			throw new IllegalArgumentException("config must be set all attributes");
		}
		loadConfig = config;
		if (this.memoryCache == null) {
			this.memoryCache = new ImageLruCache(loadConfig.memoryCacheSize);
		}
		if (this.diskCache == null) {
			this.diskCache = new ImageDiskCache(createDiskCacheDir(loadConfig.diskCacheDir));
		}
		if (this.engine == null) {
			this.engine = new ImageLoaderEngine(this.memoryCache, this.diskCache);
		}
		if (this.mHttpWorker == null) {
			this.mHttpWorker = HttpWorkerFactory.createHttpWorker();
		}
		inited = true;
	}

	/**
	 * Image disk cache saved at: /sdcard/texas/.Cache Memory cache size is:
	 * heapsize/8
	 */
	public void initDefault() {
		init(getDefaultConfig());
	}

	private static String getDefaultDiskCacheDir() {
		String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
		String cachePath = sdCard + File.separator + DEFAULT_DISK_CACHE_DIR;
		return cachePath;
	}

	private ImageLoaderConfig getDefaultConfig() {
		ImageLoaderConfig config = new ImageLoaderConfig();
		config.diskCacheDir = getDefaultDiskCacheDir();
		config.memoryCacheSize = DEFAULT_MEMORY_CACHE_SIZE;
		config.decodeOptions = getDefaultOptions();
		return config;
	}

	private static File createDiskCacheDir(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		File file = new File(path);
		if (file.exists() && file.isDirectory()) {
			return file;
		} else {
			if (file.mkdirs()) {
				return file;
			} else {
				return null;
			}
		}
	}

	public void load(final String imageUrl, ImageView view) {
		load(imageUrl, new ImageViewWrapper(view), ImageLoader.getImageLoadListener(imageUrl, view, 0, 0), null);
	}
	
	public void load(final String imageUrl, ImageView view, Options decodeOptions) {
		load(imageUrl, new ImageViewWrapper(view), ImageLoader.getImageLoadListener(imageUrl, view, 0, 0), decodeOptions);
	}

	public void load(final String imageUrl, ImageView view, int defaultImage) {
		load(imageUrl, new ImageViewWrapper(view), ImageLoader.getImageLoadListener(imageUrl, view, defaultImage, 0), null);
	}

	public void load(final String imageUrl, ImageView view, int defaultImage, int errorImage) {
		load(imageUrl, new ImageViewWrapper(view),
				ImageLoader.getImageLoadListener(imageUrl, view, defaultImage, errorImage), null);
	}
	
	public void load(final String imageUrl, ImageView view, final ImageLoadListener listener) {
		load(imageUrl, new ImageViewWrapper(view), listener, null);
	}

	private void load(final String url, final ImageViewWrapper imageWrapper, final ImageLoadListener listener, Options options) {
		if (!inited) {
			throw new IllegalStateException("ImageLoader must be init with ImageLoaderConfig before use");
		}
		throwIfNotInMainThread();
		if (TextUtils.isEmpty(url)) {
			engine.cancelDisplayTaskFor(imageWrapper);
			return;
		}
		if (listener == null)
			return;
		final String cacheKey = MD5Util.generateMD5(url);
		Bitmap cachedBitmap = engine.getFromMemoryCache(cacheKey);
		if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
			HLog.d("Load Image from memory in MainThread, URL:" + url);
			listener.onSuccess(url, imageWrapper.getImageView(), cachedBitmap, LoadFrom.FROM_MEMORY);
			engine.cancelDisplayTaskFor(imageWrapper);
			return;
		}
		listener.onSuccess(url, imageWrapper.imageViewRef.get(), null, LoadFrom.FROM_MEMORY);
		engine.prepareDisplayTaskFor(imageWrapper, cacheKey);
		
		if (options == null) {
			if (loadConfig.decodeOptions != null) {
				options = loadConfig.decodeOptions;
			} else {
				options = getDefaultOptions();
			}
		}
		ImageLoadingInfo loadingInfo = new ImageLoadingInfo(url, imageWrapper, listener, cacheKey,
				engine.getLockForUri(url), options);
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

	/**
	 * 返回默认的ImageLoadListener
	 * 
	 * @param view
	 * @param defaultImageResId
	 *            默认图片, 0为不设置
	 * @param errorImageResId
	 *            下载失败后的图片， 0为不设置
	 * @return
	 */
	public static ImageLoadListener getImageLoadListener(final String url, final ImageView view,
			final int defaultImageResId, final int errorImageResId) {
		return new ImageLoadListener() {
			@Override
			public void onError(HError error) {
				if (errorImageResId != 0) {
					view.setImageResource(errorImageResId);
				}
			}

			@Override
			public void onSuccess(String url, ImageView view, Bitmap bitmap, LoadFrom loadFrom) {
				if (bitmap != null) {
					if (loadFrom == LoadFrom.FROM_INTERNET) {
						AlphaAnimation alphaAnimation = new AlphaAnimation(0.2f, 1f);
						alphaAnimation.setDuration(1000);
						alphaAnimation.setFillAfter(true);
						view.clearAnimation();
						view.startAnimation(alphaAnimation);
					}
					view.setImageBitmap(bitmap);
				} else if (defaultImageResId != 0) {
					view.setImageResource(defaultImageResId);
				}
			}

		};
	}

	/**
	 * 默认decode options,使用RGB_565
	 */
	public static Options getDefaultOptions() {
		Options options = new Options();
		options.inPreferredConfig = Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		return options;
	}

	public void clearMemoryCache() {
		if (memoryCache != null)
			memoryCache.clear();
	}

	public void clearDiskCache() {
		if (diskCache != null)
			diskCache.clear();
	}

	static class ImageLoaderConfig {
		public String diskCacheDir; //图片磁盘缓存根路径
		public int memoryCacheSize = -1; //图片内存缓存大小,单位(byte)
		public Options decodeOptions; //默认的图片decode参数
	}
}
