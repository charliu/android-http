package com.boyaa.texas.mvc;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.boyaa.texas.http.Error;
import com.boyaa.texas.http.HttpExecutor;
import com.boyaa.texas.http.Request.RequestMethod;
import com.boyaa.texas.http.Response.ResponseListener;
import com.boyaa.texas.http.StringRequest;

public class BusinessModel {
	public void getBaiduString(final BaseCallback<String> callback, int method, Context context) {
		
		String url = "http://www.webxml.com.cn/webservices/WeatherWebService.asmx/getSupportCity";
		Map<String, String> map = new HashMap<String, String>();
		map.put("byProvinceName", "北京");
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
		if (method == 1) {
			request.mMethod = RequestMethod.GET;
		} else {
			request.mMethod = RequestMethod.POST;
		}
		HttpExecutor.execute(context, request, true);
		
	}
}
