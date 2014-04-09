package com.boyaa.texas.http;

public class Response<T> {
	public final T result;
	public final Error error;

	public Response(T result) {
		this.result = result;
		this.error = null;
	}

	public Response(Error err) {
		this.result = null;
		this.error = err;
	}

	public interface ResponseHandler<T> {
		void onSuccess(T response);

		void onError(Error e);
	}

	public boolean isSuccess() {
		return error == null;
	}

	 /** Returns a successful response containing the parsed result. */
	public static <T> Response<T> success(T result) {
		return new Response<T>(result);
	}

	/**
	 * Returns a failed response containing the given error code and an optional
	 * localized message displayed to the user.
	 */
	public static <T> Response<T> error(Error error) {
		return new Response<T>(error);
	}

}
