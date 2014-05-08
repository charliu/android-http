package com.vimc.ahttp;

import android.util.Log;

/**
 * 自定义log类
 * @author CharLiu
 *
 */
public class HLog {

	public static void d(String msg) {
		if (Constants.LOG_D) {
			Log.d(Constants.HTTP_TAG, msg);
		}
	}
	public static void w(String msg) {
		if (Constants.LOG_W) {
			Log.w(Constants.HTTP_TAG, msg);
		}
	}

	public static void v(String msg) {
		if (Constants.LOG_V) {
			Log.v(Constants.HTTP_TAG, msg);
		}
	}

	public static void e(String msg) {
		if (Constants.LOG_E) {
			Log.e(Constants.HTTP_TAG, msg);
		}
	}
	
	public static void i(String msg) {
		if (Constants.LOG_I) {
			Log.i(Constants.HTTP_TAG, msg);
		}
	}

}
