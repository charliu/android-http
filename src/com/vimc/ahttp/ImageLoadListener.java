package com.vimc.ahttp;

import android.graphics.Bitmap;

/**
 * 图片加载回调listener
 */
public interface ImageLoadListener {
	
	public enum LoadFrom {
		FROM_INTERNET,
		FORM_SDCARD,
		FROM_MEMORY,
		FROM_RESOURCE
	}
	void onSuccess(String imageUrl, Bitmap bitmap, LoadFrom from);

	void onError(HError error);
}