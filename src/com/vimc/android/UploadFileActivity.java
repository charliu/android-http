package com.vimc.android;

import java.io.ByteArrayOutputStream;
import java.io.File;

import com.vimc.ahttp.HError;
import com.vimc.ahttp.HttpExecutor;
import com.vimc.ahttp.R;
import com.vimc.ahttp.StringRequest;
import com.vimc.ahttp.Request.RequestMethod;
import com.vimc.ahttp.Response.ResponseListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class UploadFileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_file);
	}

	public void onClickButton(View v) {
		String url = "http://172.30.5.27:8008/upload";
		if (v.getId() == R.id.upload_1) {
			StringRequest request = new StringRequest(url, null, null, new ResponseListener<String>() {
				@Override
				public void onSuccess(String response) {
					Log.d("xxx", "res:" + response);
				}

				@Override
				public void onComplete(String response) {
				}

				@Override
				public void onError(HError error) {
					Log.d("xxx", "error:" + error.getErrorMsg());
				}
			});

			request.addFileParam("img1", "a.jpg", new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
					+ "Mine" + File.separator + "a.jpg"));
			request.addParam("name", "lilei");
			request.addParam("password", "xxxxxooxx");
			request.requestMethod = RequestMethod.POST;
			HttpExecutor.execute(request);
			
		} else if (v.getId() == R.id.upload_2) {
			StringRequest request = new StringRequest(url, null, null, new ResponseListener<String>() {
				@Override
				public void onSuccess(String response) {
					Log.d("xxx", "res:" + response);
				}

				@Override
				public void onComplete(String response) {
				}

				@Override
				public void onError(HError error) {
					Log.d("xxx", "error:" + error.getErrorMsg());
				}
			});
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.android);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			
			request.addByteParam("img1", "a.jpg", baos.toByteArray());
			request.addParam("name", "lilei");
			request.addParam("password", "xxxxxooxx");
			request.requestMethod = RequestMethod.POST;
			HttpExecutor.execute(request);
		} else if (v.getId() == R.id.upload_3) {
			
		}

	}

}
