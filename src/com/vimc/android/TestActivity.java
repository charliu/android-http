package com.vimc.android;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.vimc.ahttp.Pojo;
import com.vimc.ahttp.R;
import com.vimc.ahttp.DownloadError;
import com.vimc.ahttp.HError;
import com.vimc.ahttp.FileDownloadTask;
import com.vimc.ahttp.FileDownloader;
import com.vimc.ahttp.HttpExecutor;
import com.vimc.ahttp.ImageLoader;
import com.vimc.ahttp.JsonRequest;
import com.vimc.ahttp.PojoRequest;
import com.vimc.ahttp.Response;
import com.vimc.ahttp.StringRequest;
import com.vimc.ahttp.FileDownloadTask.DownloadListener;
import com.vimc.ahttp.FileDownloadTask.FileFrom;
import com.vimc.ahttp.Request.RequestMethod;
import com.vimc.ahttp.Response.ResponseListener;
import com.vimc.android.mvc.BaseCallback;
import com.vimc.android.mvc.BusinessModel;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
	
	String fileSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "temp";
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
		ImageLoader.getInstance().initDefault();
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
	}



	public void onClickButton(View v) {
		switch (v.getId()) {
		case R.id.httpsRequest:
			httpsRequest();
			break;
		case R.id.stringRequestGet:
			model.getSiChuanWeather(new BaseCallback<String>() {
				@Override
				public void onResult(String response) {
					Toast.makeText(TestActivity.this, "String Get Rquest\n" + response, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onError(HError error) {

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
		case R.id.img_grid_view:
			startActivity(new Intent(TestActivity.this, GridImageActivity.class));
			break;
		case R.id.file_download:
			downloadFile();
			break;
		}
	}

	private void httpsRequest() {
		String httpsUrl = "https://certs.cac.washington.edu/CAtest/";
		StringRequest request = new StringRequest(httpsUrl, null, null, new ResponseListener<String>() {

			@Override
			public void onSuccess(String response) {
				Toast.makeText(TestActivity.this, response.toString(), 1).show();
			}

			@Override
			public void onError(HError error) {
				Toast.makeText(TestActivity.this, "请求错误, errorCode:" + error.getErrorCode() + " msg:" + error.getMessage(),
						1).show();
			}
		});
		HttpExecutor.execute(request, TestActivity.this, true, false);
	}

	private void jsonRequest() {
		String jsonUrl = "http://m.weather.com.cn/data/101270101.html";
		JsonRequest jsonRequest = new JsonRequest(jsonUrl, null, null, new Response.ResponseListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject response) {
				Toast.makeText(TestActivity.this, response.toString(), 1).show();
			}

			@Override
			public void onError(HError error) {
				Toast.makeText(TestActivity.this, error.getErrorMsg(), 1).show();
			}
		});
		HttpExecutor.execute(jsonRequest, TestActivity.this, true);

	}

	private void downloadFile() {
		if (downloadTask == null) {
			String url = "http://gdown.baidu.com/data/wisegame/0a02d66ad2e3e7a8/aimei_2014031801.apk";

			createDownloadTask("http://gdown.baidu.com/data/wisegame/e9f794ca59d48e93/manhuadao_34.apk");
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
		downloadTask = FileDownloader.download(fileUrl, fileSavePath, new DownloadListener() {
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

			@Override
			public void onPause(String tempFilePath, long downloadedSize, long fileTotalSize) {
				Toast.makeText(TestActivity.this,
						"暂停下载\n临时文件：" + tempFilePath + "\n已下载:" + downloadedSize + "\n总大小：" + fileTotalSize, 1).show();

			}
		});
	}

	private void postStringRequest() {
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity?name=hello";
		Map<String, String> map = new HashMap<String, String>();
		map.put("byProvinceName", "北京");
		StringRequest request = new StringRequest(url, null, map, new ResponseListener<String>() {
			@Override
			public void onSuccess(String response) {
				Toast.makeText(TestActivity.this, response, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(HError e) {
				Toast.makeText(TestActivity.this, e.getErrorMsg(), Toast.LENGTH_LONG).show();
			}
		});
		request.requestMethod = RequestMethod.POST;
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
			public void onError(HError e) {
				Toast.makeText(TestActivity.this, e.toString(), Toast.LENGTH_LONG).show();
			}
		}, TestPojo.class);
		HttpExecutor.execute(request);
	}

	ImageLoader loader = ImageLoader.getInstance();

	private void bitmapRequest() {
		String jay = "http://pic4.nipic.com/20091008/2128360_084655191316_2.jpg";
		loader.load(jay, image, R.drawable.android, R.drawable.error96);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			ImageLoader.getInstance().clearMemoryCache();
			break;
		case 2:
			ImageLoader.getInstance().clearDiskCache();
			break;
		case 3:
			File files = new File(fileSavePath);
			if (files.exists() && files.isDirectory()) {
				for (File f : files.listFiles()) {
					f.delete();
				}
			}
			downloadFileButton.setText("DownloadFile");
			downloadFileButton.setClickable(true);
			if (downloadTask != null)
				downloadTask.setCompleted(false);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, 1, "Clear Memory Cache");
		menu.add(Menu.NONE, 2, 1, "Clear Disk Cache");
		menu.add(Menu.NONE, 3, 1, "Clear Download File");
		return super.onCreateOptionsMenu(menu);
	}

}
