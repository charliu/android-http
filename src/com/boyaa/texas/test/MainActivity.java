package com.boyaa.texas.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.boyaa.texas.http.DownloadError;
import com.boyaa.texas.http.FileDownloadTask;
import com.boyaa.texas.http.FileDownloadTask.DownloadListener;
import com.boyaa.texas.http.FileDownloader;
import com.boyaa.texas.http.ImageLoader;
import com.boyaa.texas.http.PojoRequest;
import com.boyaa.texas.http.Request.RequestMethod;
import com.boyaa.texas.http.Response.ResponseListener;
import com.boyaa.texas.http.Error;
import com.boyaa.texas.http.R;
import com.boyaa.texas.http.HttpExecutor;
import com.boyaa.texas.http.StringRequest;
import com.boyaa.texas.mvc.BaseCallback;
import com.boyaa.texas.mvc.BusinessModel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ImageView image;
	private static final int GET = 1;
	private static final int POST = 2;
	private BusinessModel model;
	private ProgressBar progressBar;
	private FileDownloadTask downloadTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		image = (ImageView) findViewById(R.id.image);
		model = new BusinessModel();
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(100);
		progressBar.setVisibility(View.INVISIBLE);
	}

	public void onClickButton(View v) {
		switch (v.getId()) {
		case R.id.stringRequestGet:
			model.getBaiduString(new BaseCallback<String>() {

				@Override
				public void onResult(String response) {
					Toast.makeText(MainActivity.this, "String Get Rquest\n" + response, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onError(Error error) {

				}
			}, GET, MainActivity.this);
			break;
		case R.id.stringRequestPost:
			stringRequest(POST);
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
		case R.id.file_download:
			downloadFile();
			break;
		}
	}

	private void downloadFile() {
		if (downloadTask == null) {
			createDownloadTask("http://gdown.baidu.com/data/wisegame/0a02d66ad2e3e7a8/aimei_2014031801.apk");
		} else {
			if (downloadTask.isStoped()) {
				downloadTask.startDownload();
			} else {
				downloadTask.stopDownload();
			}
		}
	}

	private void createDownloadTask(String fileUrl) {
		downloadTask = FileDownloader.download(fileUrl, new DownloadListener() {
			@Override
			public void onUpdateProgress(long currentSize, long totalSize, int percent) {
				if (progressBar.getVisibility() == View.INVISIBLE) {
					progressBar.setVisibility(View.VISIBLE);
				}
				progressBar.setProgress(percent);
			}

			@Override
			public void onStart(String fileUrl) {

			}

			@Override
			public void onComplete(File downloadedFile) {
				Toast.makeText(MainActivity.this, "Download success, saved at:\n" + downloadedFile.getAbsolutePath(),
						1).show();
			}

			@Override
			public void onError(DownloadError error) {
				Toast.makeText(MainActivity.this, "on error:" + error.getDownloadedSize(), 1).show();
			}
		});
	}

	private void stringRequest(int method) {
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity";
		Map<String, String> map = new HashMap<String, String>();
		map.put("byProvinceName", "北京");
		StringRequest request = new StringRequest(url, null, map, new ResponseListener<String>() {
			@Override
			public void onSuccess(String response) {
				Toast.makeText(MainActivity.this, "String Test:" + response, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(Error e) {
				Toast.makeText(MainActivity.this, "error:" + e.errorDescription, Toast.LENGTH_LONG).show();
			}
		});
		if (method == 1) {
			request.mMethod = RequestMethod.GET;
		} else {
			request.mMethod = RequestMethod.POST;
		}
		HttpExecutor.execute(this, request, true);
	}

	private void pojoRequest() {
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity?byProvinceName=hello";
		PojoRequest<TestPojo> request = new PojoRequest<TestPojo>(url, null, null, new ResponseListener<TestPojo>() {

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

	ImageLoader loader = ImageLoader.getInstance();

	private void bitmapRequest() {
		// "http://h.hiphotos.baidu.com/image/w%3D2048/sign=ae39fc65544e9258a63481eea8bad158/4610b912c8fcc3ce64e7dd329045d688d43f208f.jpg";
		// String url = "http://img10.3lian.com/c1/newpic/10/34/47.jpg";
		String jay = "http://pic4.nipic.com/20091008/2128360_084655191316_2.jpg";
		loader.load(jay, image, R.drawable.ps_96, R.drawable.error96);
	}
}
