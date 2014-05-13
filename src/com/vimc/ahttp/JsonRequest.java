package com.vimc.ahttp;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vimc.ahttp.Response.ResponseListener;

/**
 * JSON请求
 * @author CharLiu
 *
 */
public class JsonRequest extends Request<JSONObject>{

	public JsonRequest(String url, Map<String, String> header, Map<String, String> params,
			ResponseListener<JSONObject> listener) {
		super(url, header, params, listener);
	}

	@Override
	public Response<JSONObject> parseResponse(byte[] data) {
		try {
			String jsonStr = new String(data);
			return Response.success(new JSONObject(jsonStr));
		} catch (JSONException e) {
			if (HLog.Config.LOG_E)
				e.printStackTrace();
			return Response.error(new Error(e, Error.PARSE_ERROR, "Parse json fail"));
		}
		
	}

}
