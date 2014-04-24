package com.vim.ahttp;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Bitmap内存缓存
 * 
 * @author CharLiu
 * 
 */
public class ImageLruCache extends Cache<Bitmap> {
	private final LruCache<String, Bitmap> bitmapCache;

	private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	// Use 1/8th of the available memory for this memory cache.
	private static final int DEFAULT_CACHE_SIZE = maxMemory / 8;

	public ImageLruCache() {
		this(DEFAULT_CACHE_SIZE);
	}

	public ImageLruCache(int maxSize) {
		bitmapCache = new BitmapLruCache(maxSize);
	}

	@Override
	public void put(String key, Bitmap bitmap) {
		bitmapCache.put(key, bitmap);
	}

	@Override
	public Bitmap get(String key) {
		return bitmapCache.get(key);
	}

	private class BitmapLruCache extends LruCache<String, Bitmap> {

		public BitmapLruCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected int sizeOf(String key, Bitmap bitmap) {
			return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
		}
	}

	@Override
	void clear() {
	}

}
