package com.boyaa.texas.http;


import com.boyaa.texas.http.R;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;

public class LoadingDialog extends Dialog {
	public LoadingDialog(Context context, int theme) {
		super(context, theme);
	}

	protected LoadingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public LoadingDialog(Context context) {
		super(context, R.style.Theme_Transparent);
		ProgressBar progressBar = new ProgressBar(context);
		addContentView(progressBar, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

}
 