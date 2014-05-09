package com.vimc.ahttp;

import android.util.Log;

/**
 * 自定义log类
 * @author CharLiu
 *
 */
public class HLog {

	public static void d(String msg) {
		if (LogConfig.LOG_D) {
			Log.d(LogConfig.HTTP_TAG, msg);
		}
	}
	public static void w(String msg) {
		if (LogConfig.LOG_W) {
			Log.w(LogConfig.HTTP_TAG, msg);
		}
	}

	public static void v(String msg) {
		if (LogConfig.LOG_V) {
			Log.v(LogConfig.HTTP_TAG, msg);
		}
	}

	public static void e(String msg) {
		if (LogConfig.LOG_E) {
			Log.e(LogConfig.HTTP_TAG, msg);
		}
	}
	
	public static void i(String msg) {
		if (LogConfig.LOG_I) {
			Log.i(LogConfig.HTTP_TAG, msg);
		}
	}

}
