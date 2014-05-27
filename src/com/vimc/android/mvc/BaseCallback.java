package com.vimc.android.mvc;

import com.vimc.ahttp.HError;

public interface BaseCallback<T> {
	void onResult(T response);

	void onError(HError error);
}
