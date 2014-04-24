package com.vimc.ahttp;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.widget.ImageView;

/**
 * 
 * @author charliu
 *
 */
public class ImageViewWrapper {

	protected Reference<ImageView> imageViewRef;

	public ImageViewWrapper(ImageView imageView) {
		imageViewRef = new WeakReference<ImageView>(imageView);
	}

	public ImageView getImageView() {
		return imageViewRef.get();
	}

	public boolean isCollected() {
		return imageViewRef.get() == null;
	}

	public int getId() {
		ImageView imageView = imageViewRef.get();
		return imageView == null ? super.hashCode() : imageView.hashCode();
	}

	public boolean setImageDrawable(Drawable drawable) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			ImageView imageView = imageViewRef.get();
			if (imageView != null) {
				imageView.setImageDrawable(drawable);
				return true;
			}
		}
		return false;
	}

	public boolean setImageBitmap(Bitmap bitmap) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			ImageView imageView = imageViewRef.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
				return true;
			}
		}
		return false;
	}
}