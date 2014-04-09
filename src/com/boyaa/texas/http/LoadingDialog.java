package com.boyaa.texas.http;


import com.boyaa.texas.http.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;

public class LoadingDialog extends Dialog {
	protected static final String TAG = "IphoneDialog";

	private View mView;

	public LoadingDialog(Context context, int theme) {
		super(context, theme);
	}

	protected LoadingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public LoadingDialog(Context context) {
		super(context, R.style.Theme_Transparent);
		mView = LayoutInflater.from(context).inflate(R.layout.progress_bar, null);
		ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.progressbar);
		progressBar.setIndeterminate(true);
		progressBar.setIndeterminateDrawable(new LoadingDrawable(context));
		setContentView(mView);
		LayoutParams a = getWindow().getAttributes();
		a.dimAmount = 0;
		getWindow().setAttributes(a);
	}

}
