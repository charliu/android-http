package com.vimc.ahttp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

/**
 * Image加载引擎，负责缓存，线程池管理
 * 
 * @author CharLiu
 * 
 */
public class ImageLoaderEngine {
	private Cache<Bitmap> memoryCache;
	private Cache<Bitmap> diskCache;
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 64;

	private Handler mHandler = new Handler(Looper.getMainLooper());

	@SuppressLint("UseSparseArrays")
	private final Map<Integer, String> cacheKeysForImageViewWrapper = Collections
			.synchronizedMap(new HashMap<Integer, String>());

	private final Executor THREAD_POOL_EXECUTOR;
	private final Executor taskDistributor;

	private final Map<String, ReentrantLock> uriLocks = new WeakHashMap<String, ReentrantLock>();

	public ImageLoaderEngine(Cache<Bitmap> memoryCache, Cache<Bitmap> diskCache) {
		this.memoryCache = memoryCache;
		this.diskCache = diskCache;
		BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
		taskDistributor = Executors.newCachedThreadPool();
		THREAD_POOL_EXECUTOR = ExecutorFactory.createExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
				Thread.NORM_PRIORITY - 1, taskQueue);
	}

	public void submit(final Runnable task) {
		taskDistributor.execute(new Runnable() {
			@Override
			public void run() {
				THREAD_POOL_EXECUTOR.execute(task);
			}
		});

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

	public void putToMemoryCache(String cacheKey, Bitmap bitmap) {
		if (memoryCache != null) {
			memoryCache.put(cacheKey, bitmap);
		}
	}

	public Bitmap getFromMemoryCache(String cacheKey) {
		if (memoryCache == null)
			return null;
		return memoryCache.get(cacheKey);
	}

	public void putToDiskCache(String cacheKey, Bitmap bitmap) {
		if (diskCache != null) {
			diskCache.put(cacheKey, bitmap);
		}
	}

	public Bitmap getFromDiskCache(String cacheKey) {
		if (diskCache == null)
			return null;
		return diskCache.get(cacheKey);
	}
	
	public void clearDiskCache() {
		if (diskCache != null)
			diskCache.clear();
	}
	
	public void clearMemoryCache() {
		if (diskCache != null)
			memoryCache.clear();
	}

	public boolean isReused(ImageViewWrapper wrapper, String cacheKey) {
		String currentCacheKey = getLoadingUriForView(wrapper);
		return !cacheKey.equals(currentCacheKey);
	}

	String getLoadingUriForView(ImageViewWrapper wrapper) {
		return cacheKeysForImageViewWrapper.get(wrapper.getId());
	}

}
