package com.vimc.ahttp;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.vimc.ahttp.Response.ResponseListener;

/**
 * 字符串请求
 * 
 * @author CharLiu
 * 
 */
public class StringRequest extends Request<String> {

	public StringRequest(String url, Map<String, String> header, Map<String, String> params, ResponseListener<String> listener) {
		super(url, header, params, listener);
	}

	@Override
	public Response<String> parseResponse(NetworkResponse response) {
		String parsed;
		try {
			parsed = new String(response.data, response.getCharset());
		} catch (UnsupportedEncodingException e) {
			parsed = new String(response.data);
		}
		return Response.success(parsed);
	}
}
