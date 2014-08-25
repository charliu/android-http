package com.vimc.ahttp;

import java.io.UnsupportedEncodingException;
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
	public Response<JSONObject> parseResponse(NetworkResponse response) {
		try {
			String parsed;
	        try {
	            parsed = new String(response.data, response.getCharset());
	        } catch (UnsupportedEncodingException e) {
	            parsed = new String(response.data);
	        }
			return Response.success(new JSONObject(parsed));
		} catch (JSONException e) {
			if (HLog.Config.LOG_E)
				e.printStackTrace();
			return Response.error(new HError(e, HError.PARSE_ERROR, "Parse json fail"));
		}
		
	}

}
