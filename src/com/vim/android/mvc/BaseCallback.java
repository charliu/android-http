package com.vim.android.mvc;

import com.vim.ahttp.Error;

public interface BaseCallback<T> {
	void onResult(T response);

	void onError(Error error);
}
