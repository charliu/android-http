package com.boyaa.texas.http;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.boyaa.texas.http.Response.ResponseHandler;

public class BitmapRequest extends Request<Bitmap> {

	private static final byte[] sDecodeLock = new byte[1]; // 一次只decode一张图片

	private Config mConfig;

	public BitmapRequest(String url, ResponseHandler<Bitmap> handler) {
		super(url, null, null, handler);
	}

	public Bitmap mResponse;

	@Override
	public Response<Bitmap> parseResponse(byte[] data) {
		Options options = new Options();
		options.inPreferredConfig = mConfig;
		synchronized (sDecodeLock) {
			try {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				if (bitmap != null) {
					return Response.success(bitmap);
				} else {
					return Response.error(new Error(Error.PARSE_ERROR, "decode bitmap fail"));
				}
			} catch (OutOfMemoryError e) {
				return Response.error(new Error(Error.PARSE_ERROR, "out of memory error"));
			}
		}
	}

}
