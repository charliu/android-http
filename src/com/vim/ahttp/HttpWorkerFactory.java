package com.vim.ahttp;

import android.net.http.AndroidHttpClient;
import android.os.Build;

/**
 * 根据当前SDK版本创建合适的HttpWorker
 * 
 * @author CharLiu
 */
public final class HttpWorkerFactory {

	public static HttpWorker createHttpWorker() {
//		return new HurlWorker();
//		return new HttpClientWorker(AndroidHttpClient.newInstance("android"));
		if (Build.VERSION.SDK_INT >= 9) {
			return new HurlWorker();
		} else {
			// Prior to Gingerbread, HttpUrlConnection was unreliable.
			// See:
			// http://android-developers.blogspot.com/2011/09/androids-http-clients.html
			return new HttpClientWorker(AndroidHttpClient.newInstance("android"));
		}
	}

}
