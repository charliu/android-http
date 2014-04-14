package com.boyaa.texas.http;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.boyaa.texas.http.Response.ResponseHandler;

public class JsonRequest extends Request<JSONObject>{

	public JsonRequest(String url, Map<String, String> header, Map<String, String> params,
			ResponseHandler<JSONObject> handler) {
		super(url, header, params, handler);
	}

	@Override
	public Response<JSONObject> parseResponse(byte[] data) {
		try {
			String jsonStr = new String(data);
			return Response.success(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.error(new Error(Error.PARSE_ERROR, "parse error"));
	}

}
