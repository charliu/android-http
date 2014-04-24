package com.vimc.android.mvc;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.vimc.ahttp.Error;
import com.vimc.ahttp.HttpExecutor;
import com.vimc.ahttp.StringRequest;
import com.vimc.ahttp.Request.RequestMethod;
import com.vimc.ahttp.Response.ResponseListener;

public class BusinessModel {
	public void getSiChuanWeather(final BaseCallback<String> callback, Context context) {

		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity";
		Map<String, String> map = new HashMap<String, String>();
		map.put("byProvinceName", "四川");
		StringRequest request = new StringRequest(url, null, map, new ResponseListener<String>() {
			@Override
			public void onSuccess(String response) {
				callback.onResult(response);
			}

			@Override
			public void onError(Error e) {
				callback.onError(e);
			}
		});
		request.mMethod = RequestMethod.GET;
		HttpExecutor.execute(request, context, true);

	}
}
