package com.boyaa.texas.http;

import android.net.http.AndroidHttpClient;
import android.os.Build;

public final class HttpWorkerFactory {

	public static HttpWorker createHttpWorker() {

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
