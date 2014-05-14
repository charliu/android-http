package com.vimc.ahttp;

import java.util.concurrent.locks.ReentrantLock;

import android.graphics.BitmapFactory.Options;

import com.vimc.ahttp.ImageLoader.ImageLoadListener;

/**
 * Loading图片信息封装
 * 
 * @author CharLiu
 * 
 */
public class ImageLoadingInfo {
	final String uri;
	final String cacheKey;
	final ImageViewWrapper imageWrapper;
	final ImageLoadListener listener;
	final ReentrantLock mLock;
	final Options decodeOptions;

	public ImageLoadingInfo(String uri, ImageViewWrapper imageWrapper, ImageLoadListener listener, String cacheKey,
			ReentrantLock lock, Options options) {
		this.uri = uri;
		this.imageWrapper = imageWrapper;
		this.listener = listener;
		this.cacheKey = cacheKey;
		this.mLock = lock;
		this.decodeOptions = options;
	}
}
