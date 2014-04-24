package com.boyaa.texas.http;

import java.util.Map;

import com.boyaa.texas.http.Response.ResponseListener;

public class PojoRequest<T extends Pojo> extends Request<T>{
	
	Class<T> cls;
	
	public PojoRequest(String url, Map<String, String> header, Map<String, String> params, ResponseListener<T> listener, Class<T> cls) {
		super(url, header, params, listener);
		this.cls = cls;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Response<T> parseResponse(byte[] data) {
		try {
			T mCls = cls.newInstance();
			Pojo result = mCls.parse(new String(data));
			return Response.success((T)result);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Response.error(new Error(Error.PARSE_ERROR, "parse error"));
	}

}
