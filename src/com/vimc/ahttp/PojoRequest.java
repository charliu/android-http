package com.vimc.ahttp;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.vimc.ahttp.Response.ResponseListener;

public class PojoRequest<T extends Pojo> extends Request<T>{
	
	Class<T> cls;
	
	public PojoRequest(String url, Map<String, String> header, Map<String, String> params, ResponseListener<T> listener, Class<T> cls) {
		super(url, header, params, listener);
		this.cls = cls;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Response<T> parseResponse(NetworkResponse response) {
		try {
			String parsed;
	        try {
	            parsed = new String(response.data, response.getCharset());
	        } catch (UnsupportedEncodingException e) {
	            parsed = new String(response.data);
	        }
	        if (parsed != null) {
				T mCls = cls.newInstance();
				Pojo result = mCls.parse(new String(response.data));
				return Response.success((T)result);
	        }
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Response.error(new HError(HError.PARSE_ERROR, "parse error"));
	}

}
