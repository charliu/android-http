package com.vimc.ahttp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.os.Looper;

/**
 * HttpRequest执行者
 * 
 * @author CharLiu
 * 
 */
public class HttpExecutor {
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

	private final static HttpWorker mHttpWorker = HttpWorkerFactory.createHttpWorker();

	private static ResponsePoster mPoster = new ResponsePoster(new Handler(Looper.getMainLooper()));

	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

	public static final Executor HTTP_THREAD_POOL_EXECUTOR = ExecutorFactory.createExecutor(CORE_POOL_SIZE,
			MAXIMUM_POOL_SIZE, THREAD_PRIORITY, sPoolWorkQueue);

	public static void execute(Request<?> request) {
		HttpTask task = new HttpTask(request, mPoster, mHttpWorker);
		HTTP_THREAD_POOL_EXECUTOR.execute(task);
	}

	/**
	 * execute http request
	 * 
	 * @param request
	 * @param context
	 * @param showProgressBar
	 *            是否显示加载对话框
	 */
	public static void execute(Request<?> request, Activity context, boolean showProgressBar) {
		request.dialog = createLoadingDialog(context, request, true);
		HttpTask task = new HttpTask(request, mPoster, mHttpWorker);
		if (showProgressBar) {
			request.dialog.show();
		}
		HTTP_THREAD_POOL_EXECUTOR.execute(task);
	}

	/**
	 * execute http request
	 * 
	 * @param request
	 * @param context
	 * @param showProgressBar
	 *            是否显示加载对话框
	 */
	public static void execute(Request<?> request, Activity context, boolean showProgressBar, boolean canAbort) {
		request.dialog = createLoadingDialog(context, request, canAbort);
		HttpTask task = new HttpTask(request, mPoster, mHttpWorker);
		if (showProgressBar) {
			request.dialog.show();
		}
		HTTP_THREAD_POOL_EXECUTOR.execute(task);
	}

	private static Dialog createLoadingDialog(Activity context, final Request<?> request, final boolean canAbort) {
		Dialog dialog = new LoadingDialog(context);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(canAbort);
		if (canAbort) {
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dialog.dismiss();
					request.cancel();
				}
			});
		}
		return dialog;
	}

}
