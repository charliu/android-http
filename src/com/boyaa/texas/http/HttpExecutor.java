package com.boyaa.texas.http;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

public class HttpExecutor {
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int KEEP_ALIVE = 1;

	private final static HttpWorker mWorker;
	static {
		if (Build.VERSION.SDK_INT >= 9) {
			mWorker = new HurlWorker();
		} else {
			// Prior to Gingerbread, HttpUrlConnection was unreliable.
			// See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
			mWorker = new HttpClientWorker(AndroidHttpClient.newInstance("android"));
		}
	}

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, "HttpRquest task #" + mCount.getAndIncrement());
			thread.setPriority(Thread.NORM_PRIORITY - 2);
			return thread;
		}
	};
	/**
	 * Response
	 */
	private static ResponsePoster mPoster = new ResponsePoster(new Handler(Looper.getMainLooper()));

	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
			KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);


	public static void execute(Request<?> request) {
		HttpTask task = new HttpTask(request, mPoster, mWorker);
		THREAD_POOL_EXECUTOR.execute(task);
	}
	
	
	public static void execute(Context context, Request<?> request, boolean showProgressBar) {
		request.dialog = createLoadingDialog(context, request);
		HttpTask task = new HttpTask(request, mPoster, mWorker);
		if (showProgressBar) {
			request.dialog.show();
		}
		THREAD_POOL_EXECUTOR.execute(task);
	}
	
	private static Dialog createLoadingDialog(Context context, final Request<?> request) {
		Dialog dialog = new LoadingDialog(context);
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				request.cancel();
			}
		});
		return dialog;
	}

}
