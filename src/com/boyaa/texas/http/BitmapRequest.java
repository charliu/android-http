package com.boyaa.texas.http;

import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.boyaa.texas.http.Response.ResponseHandler;

public class BitmapRequest extends Request<Bitmap>{
	public BitmapRequest(String url, Map<String, String> header, Map<String, String> params,
			ResponseHandler<Bitmap> handler) {
		super(url, header, params, handler);
	}

	@Override
	public Response<Bitmap> parseResponse(byte[] data) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		return Response.success(bitmap);
	}

}
