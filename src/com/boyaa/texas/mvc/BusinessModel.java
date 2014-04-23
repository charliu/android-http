package com.boyaa.texas.mvc;

import com.boyaa.texas.http.Error;
import com.boyaa.texas.http.HttpExecutor;
import com.boyaa.texas.http.Response.ResponseHandler;
import com.boyaa.texas.http.StringRequest;

public class BusinessModel {
	public void getBaiduString(final BaseCallback<String> callback) {
		StringRequest request = new StringRequest("http://www.baidu.com", null, null, new ResponseHandler<String>() {
			@Override
			public void onSuccess(String response) {
				callback.onResult(response);
			}

			@Override
			public void onError(Error e) {
				callback.onError(e);
			}
		});
		HttpExecutor.execute(request);
	}
}
