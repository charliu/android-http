package com.boyaa.texas.http;

import java.io.File;
import java.util.Map;

import com.boyaa.texas.http.Response.ResponseHandler;

public class FileRequest extends Request<File>{

	
	public FileRequest(String fileUrl) {
		super(fileUrl, null, null, null);
	}
	
	public FileRequest(String url, Map<String, String> header) {
		super(url, header, null, null);
	}
	
	public FileRequest(String url, Map<String, String> header, Map<String, String> params, ResponseHandler<File> handler) {
		super(url, header, params, handler);
	}

	@Override
	public Response<File> parseResponse(byte[] data) {
		return null;
	}

}
