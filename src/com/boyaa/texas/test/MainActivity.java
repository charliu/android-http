package com.boyaa.texas.test;


import com.boyaa.texas.http.Response.ResponseHandler;
import com.boyaa.texas.http.Error;
import com.boyaa.texas.http.R;
import com.boyaa.texas.http.RequestExecutor;
import com.boyaa.texas.http.StringRequest;

import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
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
		
		StringRequest req = new StringRequest("http://www.baidu.com", null, null, new ResponseHandler<String>() {
			@Override
			public void onSuccess(String response) {
				Toast.makeText(MainActivity.this, "text:" + response, 1).show();
			}

			@Override
			public void onError(Error e) {
				
			}
		});
		RequestExecutor.execute(req, true);
		
		Log.v("xxxxxxxxxx", "onCreate");
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
