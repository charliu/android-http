package com.boyaa.texas.test;


import com.boyaa.texas.http.BitmapRequest;
import com.boyaa.texas.http.PojoRequest;
import com.boyaa.texas.http.Response.ResponseHandler;
import com.boyaa.texas.http.Error;
import com.boyaa.texas.http.R;
import com.boyaa.texas.http.RequestExecutor;
import com.boyaa.texas.http.StringRequest;

import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ImageView image;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		image = (ImageView) findViewById(R.id.image);
	}
	
	public void onClickButton(View v) {
		switch(v.getId()) {
		case R.id.stringRequest:
			stringRequest();
			break;
		case R.id.pojoRequest:
			pojoRequest();
			break;
		case R.id.bitmapRequest:
			bitmapRequest();
			break;
		case R.id.fileRequest:
			break;
		}
	}
	
	
	private void stringRequest() {
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity?byProvinceName=hello";
		StringRequest request = new StringRequest(url, null, null, new ResponseHandler<String>() {
			@Override
			public void onSuccess(String response) {
				Toast.makeText(MainActivity.this, "text:" + response, 1).show();
			}

			@Override
			public void onError(Error e) {
				
			}
		});
		RequestExecutor.execute(request, true);
	}
	
	private void pojoRequest() {
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity?byProvinceName=hello";
		PojoRequest<TestPojo> request = new PojoRequest<TestPojo>(url, null, null, new ResponseHandler<TestPojo>() {

			@Override
			public void onSuccess(TestPojo response) {
				Log.v("pojo test", "msg:" + response.toString());
				Toast.makeText(MainActivity.this, "text:" + response.toString(), 1).show();
			}

			@Override
			public void onError(Error e) {
				Toast.makeText(MainActivity.this, "text:" + e.toString(), 1).show();
			}
		}, TestPojo.class);
		RequestExecutor.execute(request, true);
	}
	
	private void bitmapRequest() {
		String url = "http://h.hiphotos.baidu.com/image/w%3D2048/sign=ae39fc65544e9258a63481eea8bad158/4610b912c8fcc3ce64e7dd329045d688d43f208f.jpg";
		BitmapRequest request = new BitmapRequest(url, null,null, new ResponseHandler<Bitmap>() {
			@Override
			public void onSuccess(Bitmap response) {
				image.setImageBitmap(response);
			}

			@Override
			public void onError(Error e) {
				Toast.makeText(MainActivity.this, "text:" + e.toString(), 1).show();
			}
		});
		RequestExecutor.execute(request, true);
	}
	
	

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.v("xxxxxxxxxx", "onStart");
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.v("xxxxxxxxxx", "onRestart");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.v("xxxxxxxxxx", "onResume");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.v("xxxxxxxxxx", "onPause");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.v("xxxxxxxxxx", "onStop");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("xxxxxxxxxx", "onDestroy");
	}
	
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.v("xxxxxxxxxx", "onConfigurationChanged");
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		Log.v("xxxxxxxxxx", "onWindowFocusChanged");
	}
	
	
	

}
