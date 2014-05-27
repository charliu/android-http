package com.vimc.ahttp;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 图片加载回调listener
 */
public interface ImageLoadListener {
	
	public enum LoadFrom {
		FROM_INTERNET,
		FORM_SDCARD,
		FROM_MEMORY
	}
	void onSuccess(String imageUrl, ImageView imageView, Bitmap bitmap, LoadFrom from);

	void onError(HError error);
}