package com.boyaa.texas.http;

import java.util.concurrent.Executor;

import android.os.Handler;

/**
 * 请求返回结果转发
 * @author CharLiu
 *
 */
public class ResponsePoster {
	private final Executor responsePoster;

	public ResponsePoster(final Handler handler) {
		responsePoster = new Executor() {
			@Override
			public void execute(Runnable command) {
				handler.post(command);
			}
		};
	}

	void dispatchResponse(Request<?> request, Response<?> response) {
		responsePoster.execute(new ResponsePosterRunnable(request, response));
	}

	@SuppressWarnings("rawtypes")
	private class ResponsePosterRunnable implements Runnable {
		private final Request mRequest;
		private final Response mResponse;

		public ResponsePosterRunnable(Request request, Response response) {
			mRequest = request;
			mResponse = response;

		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			if (mRequest.dialog != null && mRequest.dialog.isShowing()) {
				mRequest.dialog.dismiss();
			}
			if (mResponse.error == null) {
				mRequest.dispatchResponse(mResponse.result);
			} else {
				mRequest.dispatchError(mResponse.error);
			}
		}
	}

}
