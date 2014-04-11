package com.boyaa.texas.http;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.boyaa.texas.http.Response.ResponseHandler;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {

	private ImageCache imageCache;
	private Map<String, ImageRequest> mInFlightRequests = new HashMap<String, ImageRequest>();

	private final HashMap<String, ImageRequest> mBatchedResponses = new HashMap<String, ImageRequest>();

	private final Handler mHandler = new Handler(Looper.getMainLooper());

	public ImageLoader(ImageCache cache) {
		this.imageCache = cache;
	}

	public interface ImageCache {
		void put(String key, Bitmap bitmap);

		Bitmap get(String key);
	}

	public interface ImageHandler {
		void onSuccess(ImageWrapper wrapper);

		void onError(Error error);
	}
	
	public void load(final String imageUrl, ImageView view) {
		load(imageUrl, view, 0);
	}
	
	public void load(final String imageUrl, ImageView view, int defaultImage) {
		load(imageUrl, view, defaultImage, 0);
	}
	
	public void load(final String imageUrl, ImageView view, int defaultImage, int errorImage) {
		load(imageUrl, view, ImageLoader.getImageHandler(imageUrl, view, defaultImage, errorImage));
	}

	public void load(final String imageUrl, ImageView view, final ImageHandler imageHandler) {
		throwIfNotInMainThread();
		view.setTag(imageUrl);
		
		if (TextUtils.isEmpty(imageUrl))
			return;
		Bitmap cachedBitmap = getFromCache(imageUrl);
		if (cachedBitmap != null) {
			ImageWrapper container = new ImageWrapper(cachedBitmap, imageUrl, null, null);
			imageHandler.onSuccess(container);
			Log.d("Cache", "Load Image from cache");
			return;
		}
		if (mInFlightRequests.get(imageUrl) != null) {
			Log.d("Cache", "Request already in map");
			return;
		}

		ImageWrapper imageWrapper = new ImageWrapper(null, imageUrl, imageUrl, imageHandler);

		// Update the caller to let them know that they should use the default
		// bitmap.
		imageHandler.onSuccess(imageWrapper);

		// Check to see if a request is already in-flight.
		ImageRequest imgRequest = mInFlightRequests.get(imageUrl);
		if (imgRequest != null) {
			// If it is, add this request to the list of listeners.
			imgRequest.addWrapper(imageWrapper);
			return;
		}
		imageHandler.onSuccess(imageWrapper); // set default image

		BitmapRequest request = new BitmapRequest(imageUrl, new ResponseHandler<Bitmap>() {
			@Override
			public void onSuccess(Bitmap response) {
				onGetImageSuccess(imageUrl, response);
			}

			@Override
			public void onError(Error error) {
				onGetImageError(imageUrl, error);
			}
		});

		mInFlightRequests.put(imageUrl, new ImageRequest(request, imageWrapper));

		HttpExecutor.execute(request);
	}

	private void onGetImageSuccess(String cacheKey, Bitmap bitmap) {
		putToCache(cacheKey, bitmap);
		ImageRequest request = mInFlightRequests.remove(cacheKey);
		if (request != null) {
			request.responseBitmap = bitmap;
			mBatchedResponses.put(cacheKey, request);
			batchResponse(cacheKey, request);
		}
	}

	private void onGetImageError(String cacheKey, Error error) {
		ImageRequest request = mInFlightRequests.remove(cacheKey);
		if (request != null) {
			request.setError(error);
			batchResponse(cacheKey, request);
		}
	}

	private Runnable mRunnable;

	private void batchResponse(String cacheKey, ImageRequest request) {
		mBatchedResponses.put(cacheKey, request);
		// If we don't already have a batch delivery runnable in flight, make a
		// new one.
		// Note that this will be used to deliver responses to all callers in
		// mBatchedResponses.
		if (mRunnable == null) {
			mRunnable = new Runnable() {
				@Override
				public void run() {
					for (ImageRequest request : mBatchedResponses.values()) {
						for (ImageWrapper wrapper : request.mWrappers) {
							// If one of the callers in the batched request
							// canceled the request
							// after the response was received but before it was
							// delivered,
							// skip them.
							if (wrapper.mHandler == null) {
								continue;
							}
							if (request.getError() == null) {
								wrapper.mBitmap = request.responseBitmap;
								wrapper.mHandler.onSuccess(wrapper);
							} else {
								wrapper.mHandler.onError(request.getError());
							}
						}
					}
					mBatchedResponses.clear();
					mRunnable = null;
				}

			};
			mHandler.postDelayed(mRunnable, 100);
		}
	}

	private class ImageRequest {
		private Request<?> mRequest;
		private Bitmap responseBitmap;
		private Error mError;

		private final LinkedList<ImageWrapper> mWrappers = new LinkedList<ImageWrapper>();

		public ImageRequest(Request<?> request, ImageWrapper wrapper) {
			mRequest = request;
			mWrappers.add(wrapper);
		}

		public void addWrapper(ImageWrapper wrapper) {
			mWrappers.add(wrapper);
		}

		public Error getError() {
			return mError;
		}

		public void setError(Error error) {
			mError = error;
		}

		public boolean removeWrapperAndCancelIfNecessary(ImageWrapper wrapper) {
			mWrappers.remove(wrapper);
			if (mWrappers.size() == 0) {
				mRequest.cancel();
				return true;
			}
			return false;
		}

	}

	public class ImageWrapper {
		private Bitmap mBitmap;
		private final ImageHandler mHandler;
		private final String mCacheKey;
		private final String mRequestUrl;

		public ImageWrapper(Bitmap bitmap, String requestUrl, String cacheKey, ImageHandler handler) {
			mBitmap = bitmap;
			mRequestUrl = requestUrl;
			mCacheKey = cacheKey;
			mHandler = handler;
		}

		public void cancelRequest() {
			if (mHandler == null) {
				return;
			}

			ImageRequest request = mInFlightRequests.get(mCacheKey);
			if (request != null) {
				boolean canceled = request.removeWrapperAndCancelIfNecessary(this);
				if (canceled) {
					mInFlightRequests.remove(mCacheKey);
				}
			} else {
				// check to see if it is already batched for delivery.
				request = mBatchedResponses.get(mCacheKey);
				if (request != null) {
					request.removeWrapperAndCancelIfNecessary(this);
					if (request.mWrappers.size() == 0) {
						mBatchedResponses.remove(mCacheKey);
					}
				}
			}
		}
		
		public Bitmap getBitmap() {
			return mBitmap;
		}
		
		public String getRequestUrl() {
			return mRequestUrl;
		}
	}

	/**
	 * 判断当前是否为主线程调用
	 */
	private void throwIfNotInMainThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("ImageLoader must be call from the main thread.");
		}
	}

	private void putToCache(String key, Bitmap bitmap) {
		if (imageCache != null) {
			imageCache.put(key, bitmap);
		}
	}

	private Bitmap getFromCache(String key) {
		if (imageCache == null)
			return null;
		return imageCache.get(key);
	}
	
	/**
	 * 返回默认的ImageHandler
	 * 
	 * @param view
	 * @param defaultImageResId
	 *            默认图片
	 * @param errorImageResId
	 *            下载失败后的图片
	 * @return
	 */
	public static ImageHandler getImageHandler(final String url, final ImageView view, final int defaultImageResId,
			final int errorImageResId) {
		return new ImageHandler() {
			@Override
			public void onError(Error error) {
				if (errorImageResId != 0) {
					view.setImageResource(errorImageResId);
				}
			}

			@Override
			public void onSuccess(ImageWrapper response) {
				if (response.mBitmap != null) {
					Object tag = view.getTag();
					if (tag != null && tag.equals(url)) 
						view.setImageBitmap(response.mBitmap);
				} else if (defaultImageResId != 0) {
					view.setImageResource(defaultImageResId);
				}
			}
		};
	}
}
