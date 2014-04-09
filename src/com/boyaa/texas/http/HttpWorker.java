package com.boyaa.texas.http;

import java.io.IOException;

import org.apache.http.HttpResponse;

public interface HttpWorker {
	HttpResponse doHttpRquest(Request<?> request) throws IOException;
	
}
