package com.boyaa.texas.http;

import com.boyaa.texas.http.ImageLoader.ImageLoadListener;

public class ImageLoadingInfo {
	final String uri;
	final String cacheKey;
	final ImageViewWrapper imageWrapper;
	final ImageLoadListener listener;

	public ImageLoadingInfo(String uri, ImageViewWrapper imageWrapper, ImageLoadListener listener, String cacheKey) {
		this.uri = uri;
		this.imageWrapper = imageWrapper;
		this.listener = listener;
		this.cacheKey = cacheKey;
	}
}
