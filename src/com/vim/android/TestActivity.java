package com.vim.android;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.vim.ahttp.FileDownloadTask.FileFrom;
import com.vim.ahttp.JsonRequest;
import com.vim.ahttp.R;
import com.vim.ahttp.DownloadError;
import com.vim.ahttp.Error;
import com.vim.ahttp.FileDownloadTask;
import com.vim.ahttp.FileDownloader;
import com.vim.ahttp.HttpExecutor;
import com.vim.ahttp.ImageLoader;
import com.vim.ahttp.PojoRequest;
import com.vim.ahttp.Response;
import com.vim.ahttp.StringRequest;
import com.vim.ahttp.FileDownloadTask.DownloadListener;
import com.vim.ahttp.Request.RequestMethod;
import com.vim.ahttp.Response.ResponseListener;
import com.vim.android.mvc.BaseCallback;
import com.vim.android.mvc.BusinessModel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * TestActivity
 * 
 * @author CharLiu
 * 
 */
public class TestActivity extends Activity {
	private ImageView image;
	private BusinessModel model;
	private ProgressBar progressBar;
	private FileDownloadTask downloadTask;
	private Button downloadFileButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		image = (ImageView) findViewById(R.id.image);
		model = new BusinessModel();
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(100);
		progressBar.setVisibility(View.INVISIBLE);
		downloadFileButton = (Button) findViewById(R.id.file_download);
	}

	public void onClickButton(View v) {
		switch (v.getId()) {
		case R.id.stringRequestGet:
			model.getSiChuanWeather(new BaseCallback<String>() {
				@Override
				public void onResult(String response) {
					Toast.makeText(TestActivity.this, "String Get Rquest\n" + response, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onError(Error error) {

				}
			}, TestActivity.this);
			break;
		case R.id.stringRequestPost:
			postStringRequest();
			break;
		case R.id.jsonRequest:
			jsonRequest();
			break;
		case R.id.pojoRequest:
			pojoRequest();
			break;
		case R.id.bitmapRequest:
			bitmapRequest();
			break;
		case R.id.img_list_view:
			Intent intent = new Intent(TestActivity.this, ListImageActivity.class);
			startActivity(intent);
			break;
		case R.id.file_download:
			downloadFile();
			break;
		}
	}

	private void jsonRequest() {
		String jsonUrl = "http://m.weather.com.cn/data/101270101.html";
		JsonRequest jsonRequest = new JsonRequest(jsonUrl, null, null, new Response.ResponseListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject response) {
				Toast.makeText(TestActivity.this, response.toString(), 1).show();
			}

			@Override
			public void onError(Error error) {
				Toast.makeText(TestActivity.this, error.errorDescription, 1).show();
			}
		});
		HttpExecutor.execute(jsonRequest, TestActivity.this, true);

	}

	private void downloadFile() {
		if (downloadTask == null) {
			createDownloadTask("http://gdown.baidu.com/data/wisegame/0a02d66ad2e3e7a8/aimei_2014031801.apk");
			downloadFileButton.setText("Pause Download");
		} else {
			if (!downloadTask.isCompleted()) {
				if (downloadTask.isStoped()) {
					downloadTask.startDownload();
					downloadFileButton.setText("Pause Download");
				} else {
					downloadTask.stopDownload();
					downloadFileButton.setText("Continue Download");
				}
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
			public void onComplete(File downloadedFile, FileDownloadTask.FileFrom from) {
				Toast.makeText(TestActivity.this, "Download success, saved at:\n" + downloadedFile.getAbsolutePath(), 1)
						.show();
				if (progressBar.getVisibility() == View.VISIBLE) {
					progressBar.setVisibility(View.INVISIBLE);
				}
				if (from == FileFrom.INTERNET) {
					downloadFileButton.setText("Download success");
				} else {
					downloadFileButton.setText("File Exist IN SdCard");
				}
				downloadFileButton.setClickable(false);
			}

			@Override
			public void onError(DownloadError error) {
				Toast.makeText(TestActivity.this, "Download failed at:" + error.getDownloadedSize() + "byte", 1).show();
			}
		});
	}

	private void postStringRequest() {
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity";
		Map<String, String> map = new HashMap<String, String>();
		map.put("byProvinceName", "北京");
		StringRequest request = new StringRequest(url, null, map, new ResponseListener<String>() {
			@Override
			public void onSuccess(String response) {
				Toast.makeText(TestActivity.this, response, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(Error e) {
				Toast.makeText(TestActivity.this, e.errorDescription, Toast.LENGTH_LONG).show();
			}
		});
		request.mMethod = RequestMethod.POST;
		HttpExecutor.execute(request, this, true);
	}

	private void pojoRequest() {
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity?byProvinceName=hello";
		PojoRequest<TestPojo> request = new PojoRequest<TestPojo>(url, null, null, new ResponseListener<TestPojo>() {

			@Override
			public void onSuccess(TestPojo response) {
				Log.v("pojo test", "msg:" + response.toString());
				Toast.makeText(TestActivity.this, response.toString(), Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(Error e) {
				Toast.makeText(TestActivity.this, e.toString(), Toast.LENGTH_LONG).show();
			}
		}, TestPojo.class);
		HttpExecutor.execute(request);
	}

	ImageLoader loader = ImageLoader.getInstance();

	private void bitmapRequest() {
		String jay = "http://pic4.nipic.com/20091008/2128360_084655191316_2.jpg";
		loader.load(jay, image, R.drawable.ps_96, R.drawable.error96);
	}
}
