package com.boyaa.texas.http;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.boyaa.texas.http.Response.ResponseListener;

/**
 * Bitmap Request
 * @author CharLiu
 *
 */
public class BitmapRequest extends Request<Bitmap> {

	private static final byte[] sDecodeLock = new byte[1]; // 一次只decode一张图片

	private Config mConfig;

	public BitmapRequest(String url) {
		super(url, null, null, null);
	}

	public BitmapRequest(String url, ResponseListener<Bitmap> listener) {
		super(url, null, null, listener);
	}

	public Bitmap mResponse;

	@Override
	public Response<Bitmap> parseResponse(byte[] data) {
		Options options = new Options();
		options.inPreferredConfig = mConfig;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		synchronized (sDecodeLock) {
			try {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				if (bitmap != null) {
					return Response.success(bitmap);
				} else {
					return Response.error(new Error(Error.PARSE_ERROR, "Decode bitmap fail"));
				}
			} catch (OutOfMemoryError e) {
				return Response.error(new Error(e, Error.PARSE_ERROR, "Parse OutOfMemoryError"));
			}
		}
	}

}
