package com.boyaa.texas.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageLoadingTask implements Runnable{
	HttpWorker httpWorker;
	
	public ImageLoadingTask(ImageLoadingInfo loadingInfo) {
		
	}
	
	@Override
	public void run() {
		
	}
	
	private Bitmap loadBitmap(String url) {
		BitmapRequest request = new BitmapRequest(url, null);
		try {
			HttpResponse response = httpWorker.doHttpRquest(request);
			byte[] data = EntityUtils.toByteArray(response.getEntity());
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
