package com.vimc.ahttp;

import java.io.File;
import java.util.Map;

import com.vimc.ahttp.Response.ResponseListener;

public class FileRequest extends Request<File>{

	
	public FileRequest(String fileUrl) {
		super(fileUrl, null, null, null);
	}
	
	public FileRequest(String url, Map<String, String> header) {
		super(url, header, null, null);
	}
	
	public FileRequest(String url, Map<String, String> header, Map<String, String> params, ResponseListener<File> listener) {
		super(url, header, params, listener);
	}

	@Override
	public Response<File> parseResponse(byte[] data) {
		return null;
	}

}
