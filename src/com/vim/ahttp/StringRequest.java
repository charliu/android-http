package com.vim.ahttp;

import java.util.Map;

import com.vim.ahttp.Response.ResponseListener;

/**
 * 字符串请求
 * 
 * @author CharLiu
 * 
 */
public class StringRequest extends Request<String> {

	public StringRequest(String url, Map<String, String> header, Map<String, String> params,
			ResponseListener<String> listener) {
		super(url, header, params, listener);
	}

	@Override
	public Response<String> parseResponse(byte[] data) {
		return Response.success(new String(data));
	}
}
