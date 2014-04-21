package com.boyaa.texas.http;

import java.util.concurrent.locks.ReentrantLock;

import com.boyaa.texas.http.ImageLoader.ImageLoadListener;

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

	public ImageLoadingInfo(String uri, ImageViewWrapper imageWrapper, ImageLoadListener listener, String cacheKey,
			ReentrantLock lock) {
		this.uri = uri;
		this.imageWrapper = imageWrapper;
		this.listener = listener;
		this.cacheKey = cacheKey;
		this.mLock = lock;
	}
}
