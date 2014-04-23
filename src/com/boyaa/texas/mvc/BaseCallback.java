package com.boyaa.texas.mvc;

import com.boyaa.texas.http.Error;

public interface BaseCallback<T> {
	void onResult(T response);

	void onError(Error error);
}
