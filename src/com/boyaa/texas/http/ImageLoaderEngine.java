package com.boyaa.texas.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

public class ImageLoaderEngine {
	Cache<Bitmap> imageCache;
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int KEEP_ALIVE = 1;

	private Handler mHandler = new Handler(Looper.getMainLooper());

	private final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, "HttpRquest task #" + mCount.getAndIncrement());
			thread.setPriority(Thread.NORM_PRIORITY - 2);
			return thread;
		}
	};

	private final Map<Integer, String> cacheKeysForImageViewWrapper = Collections
			.synchronizedMap(new HashMap<Integer, String>());

	private final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);
	public final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
			TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

	private final Map<String, ReentrantLock> uriLocks = new WeakHashMap<String, ReentrantLock>();

	private final HashMap<String, BitmapRequest> mInFlightRequests = new HashMap<String, BitmapRequest>();

	public ImageLoaderEngine(Cache<Bitmap> cache) {
		this.imageCache = cache;
	}

	public void submit(Runnable task) {
		THREAD_POOL_EXECUTOR.execute(task);
	}

	public void post(Runnable runnable) {
		mHandler.post(runnable);
	}

	public ReentrantLock getLockForUri(String uri) {
		ReentrantLock lock = uriLocks.get(uri);
		if (lock == null) {
			lock = new ReentrantLock();
			uriLocks.put(uri, lock);
		}
		return lock;
	}

	public void prepareDisplayTaskFor(ImageViewWrapper wrapper, String memoryCacheKey) {
		cacheKeysForImageViewWrapper.put(wrapper.getId(), memoryCacheKey);
	}

	public void cancelDisplayTaskFor(ImageViewWrapper wrapper) {
		cacheKeysForImageViewWrapper.remove(wrapper.getId());
	}

	public void putToCache(String cacheKey, Bitmap bitmap) {
		if (imageCache != null) {
			imageCache.put(cacheKey, bitmap);
		}
	}

	public Bitmap getFromCache(String cacheKey) {
		if (imageCache == null)
			return null;
		return imageCache.get(cacheKey);
	}

	public boolean isReused(ImageViewWrapper wrapper, String cacheKey) {
		String currentCacheKey = getLoadingUriForView(wrapper);
		return !cacheKey.equals(currentCacheKey);
	}

	String getLoadingUriForView(ImageViewWrapper wrapper) {
		return cacheKeysForImageViewWrapper.get(wrapper.getId());
	}

}
