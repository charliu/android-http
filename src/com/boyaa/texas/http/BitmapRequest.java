package com.boyaa.texas.http;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

import com.boyaa.texas.http.Response.ResponseHandler;

public class BitmapRequest extends Request<Bitmap>{
	
	private static final byte[] sDecodeLock = new byte[1]; //一次只decode一张图片
	
	private Config mConfig;
	
	public BitmapRequest(String url, ResponseHandler<Bitmap> handler) {
		super(url, null, null, handler);
	}
	
	@Override
	public Response<Bitmap> parseResponse(byte[] data) {
		Options options = new Options();
		options.inPreferredConfig = mConfig;
		synchronized (sDecodeLock) {
			Log.d("DECODE BITMAP", "decode at:" + System.currentTimeMillis());
			try {
				Bitmap bitmap = null;
					bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				return Response.success(bitmap);
			} catch (OutOfMemoryError e) {
				return Response.error(new Error(Error.PARSE_ERROR, "out of memory error"));
			}
		}
	}
	

}
