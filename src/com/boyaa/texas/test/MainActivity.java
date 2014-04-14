package com.boyaa.texas.test;

import com.boyaa.texas.http.ImageLoader;
import com.boyaa.texas.http.ImageLruCache;
import com.boyaa.texas.http.PojoRequest;
import com.boyaa.texas.http.Response.ResponseHandler;
import com.boyaa.texas.http.Error;
import com.boyaa.texas.http.R;
import com.boyaa.texas.http.HttpExecutor;
import com.boyaa.texas.http.StringRequest;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
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
		switch (v.getId()) {
		case R.id.stringRequest:
			stringRequest();
			break;
		case R.id.pojoRequest:
			pojoRequest();
			break;
		case R.id.bitmapRequest:
			bitmapRequest();
			break;
		case R.id.img_list_view:
			Intent intent = new Intent(MainActivity.this, ListViewTest.class);
			startActivity(intent);
			break;
		}
	}

	private void stringRequest() {
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity?byProvinceName=hello";
		StringRequest request = new StringRequest(url, null, null, new ResponseHandler<String>() {
			@Override
			public void onSuccess(String response) {
				Toast.makeText(MainActivity.this, "POJO Test:" + response, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(Error e) {
				Toast.makeText(MainActivity.this, "error:" + e.errorDescription, Toast.LENGTH_LONG).show();
			}
		});
		HttpExecutor.execute(this, request, true);
	}

	private void pojoRequest() {
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity?byProvinceName=hello";
		PojoRequest<TestPojo> request = new PojoRequest<TestPojo>(url, null, null, new ResponseHandler<TestPojo>() {

			@Override
			public void onSuccess(TestPojo response) {
				Log.v("pojo test", "msg:" + response.toString());
				Toast.makeText(MainActivity.this, "text:" + response.toString(), Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(Error e) {
				Toast.makeText(MainActivity.this, "error:" + e.toString(), Toast.LENGTH_LONG).show();
			}
		}, TestPojo.class);
		HttpExecutor.execute(request);
	}

	ImageLoader loader = new ImageLoader(new ImageLruCache());

	private void bitmapRequest() {
		// String url =
		// "http://h.hiphotos.baidu.com/image/w%3D2048/sign=ae39fc65544e9258a63481eea8bad158/4610b912c8fcc3ce64e7dd329045d688d43f208f.jpg";
		String url = "http://img10.3lian.com/c1/newpic/10/34/47.jpg";
		loader.load(url, image, R.drawable.ic_launcher, R.drawable.error96);
	}
}
