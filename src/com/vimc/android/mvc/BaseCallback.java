package com.vimc.android.mvc;

import com.vimc.ahttp.Error;

public interface BaseCallback<T> {
	void onResult(T response);

	void onError(Error error);
}
