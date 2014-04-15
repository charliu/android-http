package com.boyaa.texas.http;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.os.Looper;

public class HttpExecutor {
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 64;
	private static final int THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

	private final static HttpWorker mHttpWorker = HttpWorkerFactory.createHttpWorker();

	private static ResponsePoster mPoster = new ResponsePoster(new Handler(Looper.getMainLooper()));

	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

	public static final Executor THREAD_POOL_EXECUTOR = ExecutorFactory.createExecutor(CORE_POOL_SIZE,
			MAXIMUM_POOL_SIZE, THREAD_PRIORITY, sPoolWorkQueue);

	public static void execute(Request<?> request) {
		HttpTask task = new HttpTask(request, mPoster, mHttpWorker);
		THREAD_POOL_EXECUTOR.execute(task);
	}

	public static void execute(Context context, Request<?> request, boolean showProgressBar) {
		request.dialog = createLoadingDialog(context, request);
		HttpTask task = new HttpTask(request, mPoster, mHttpWorker);
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
