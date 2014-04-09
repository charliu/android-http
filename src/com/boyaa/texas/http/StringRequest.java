package com.boyaa.texas.http;

import java.util.Map;

import com.boyaa.texas.http.Response.ResponseHandler;

public class StringRequest extends Request<String>{

	public StringRequest(String url, Map<String, String> header, Map<String, String> params,
			ResponseHandler<String> handler) {
		super(url, header, params, handler);
	}

	@Override
	public Response<String> parseResponse(byte[] data) {
		return Response.success(new String(data));
	}
}
