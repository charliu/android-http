package com.boyaa.texas.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class FileDownloadTask {
	private final String fileUrl;
	private final String tempFileName;
	private final HttpWorker httpWorker;
	private final DownloadListener downloadListener;
	private Handler postHandler;

	private Thread downloadThread;
	private String fileSavePath;
	private long fileTotalSize = -1;
	private long downloadedSize = 0;
	private long readSize = 0;
	private int currentPercent = 0;
	private volatile boolean stop = false;
	private String fileName;

	FileDownloadTask(String url, String savePath, DownloadListener listener) {
		this.fileUrl = url;
		this.fileSavePath = savePath;
		httpWorker = HttpWorkerFactory.createHttpWorker();
		fileName = getFileName(url);
		tempFileName = CacheKeyUtil.generate(fileUrl);
		downloadListener = listener;
		if (downloadListener != null) {
			postHandler = new Handler(Looper.getMainLooper());
		}
	}

	public interface DownloadListener {
		void onStart(String fileUrl);

		void onUpdateProgress(long currentSize, long totalSize, int percent);

		void onComplete(File downloadedFile);
	}

	/**
	 * 
	 */
	public void startDownload() {
		if (downloadListener != null) {
			postHandler.post(new Runnable() {
				@Override
				public void run() {
					downloadListener.onStart(fileUrl);
				}
			});

		}
		downloadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				download();
			}
		});
		downloadThread.start();
	}

	public void stopDownload() {
		stop = true;
		downloadThread = null;
	}

	public boolean isStoped() {
		return stop;
	}

	private String getFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf(File.separator));
	}

	private String getSaveFilePath(String fileName) {
		return fileSavePath + File.separator + fileName;
	}

	private Map<String, String> getRangeHeaderByFile(File file) {
		Map<String, String> header = new HashMap<String, String>();
		if (file.exists()) {
			long downloadStartWith = file.length();
			downloadedSize = downloadStartWith;
			header.put("Range", "bytes=" + downloadStartWith + "-");
			Log.i(Constants.HTTP_TAG, "Http Range start with:" + downloadStartWith);
		}
		return header;
	}
	
	/**
	 * 下载文件
	 */
	private void download() {
		File existFile = checkFileExists();
		if (existFile != null && existFile.exists()) {
			Log.i(Constants.HTTP_TAG, "File already download");
			postDonwloadSuccess(existFile);
			return;
		}
		readSize = 0;
		currentPercent = 0;
		downloadedSize = 0;

		initSaveFilePath(fileSavePath);
		String fileTempPath = getSaveFilePath(tempFileName);
		File downloadTempFile = new File(fileTempPath);
		Map<String, String> headers = getRangeHeaderByFile(downloadTempFile);
		FileRequest request = new FileRequest(fileUrl, headers);

		InputStream in = null;
		RandomAccessFile raf = null;
		boolean badTempFile = false;
		try {
			HttpResponse response = httpWorker.doHttpRquest(request);
			int statusCode = response.getStatusLine().getStatusCode();
			Log.i(Constants.HTTP_TAG, "http statusCode:" + statusCode);

			if (fileTotalSize == -1 && headers.size() == 0) {
				fileTotalSize = getContentLength(response);
			}
			Log.i(Constants.HTTP_TAG, "fileTotalSize:" + fileTotalSize);
			if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
				stop = false;
				raf = new RandomAccessFile(downloadTempFile, "rw");
				raf.seek(raf.length());
				in = response.getEntity().getContent();
				byte[] buffer = new byte[1024];
				int length = 0;
				int percent = 0;
				while (!stop && (length = in.read(buffer)) != -1) {
					readSize += length;
					long downloadedTotalSize = downloadedSize + readSize;
					currentPercent = (int) ((downloadedTotalSize * 100) / fileTotalSize);
					if (currentPercent > percent) {
						Log.i(Constants.HTTP_TAG, "read size:" + downloadedTotalSize + " read percent:"
								+ currentPercent);
						postUpdateProgress();
					}
					percent = currentPercent;
					raf.write(buffer, 0, length);

				}
			} else if (statusCode == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
				badTempFile = true;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			stop = true;
			releaseConnection(in, raf);
		}
		if (badTempFile) {
			if (downloadTempFile.exists()) {
				downloadTempFile.delete();
			}
		}
		if (downloadTempFile.exists()) {
			if ((readSize + downloadedSize) == fileTotalSize) {
				final File renameFile = createRenameFile(getSaveFilePath(fileName));
				if (downloadTempFile.renameTo(renameFile)) {
					postDonwloadSuccess(renameFile);
				} else {
					Log.e(Constants.HTTP_TAG, "Rename file failed, name:" + renameFile.getAbsolutePath());
				}
			}
		}

	}

	private void postDonwloadSuccess(final File file) {
		if (downloadListener != null) {
			postHandler.post(new Runnable() {
				@Override
				public void run() {
					downloadListener.onComplete(file);
				}
			});
		}
	}

	private void postUpdateProgress() {
		if (downloadListener != null) {
			postHandler.post(new Runnable() {
				@Override
				public void run() {
					downloadListener.onUpdateProgress(readSize + downloadedSize, fileTotalSize, currentPercent);
				}
			});
		}
	}

	/**
	 * 判断文件是否已下载 根据要下载文件实际总长度与本地临时文件或已存在的同名文件SIZE作对 如果相等则认为是已经下载过了
	 * 
	 * @return
	 */
	private File checkFileExists() {
		File existFile = new File(getSaveFilePath(fileName));
		File tempFile = new File(getSaveFilePath(tempFileName));

		if ((existFile.exists() && existFile.isFile()) || (tempFile.exists() && tempFile.isFile())) {
			getFileTotalLengthByHeadRequest();
		} else {
			return null;
		}
		if (fileTotalSize != -1) {
			if (existFile.exists() && existFile.isFile()) {
				if (existFile.length() == fileTotalSize) {
					// 当存在的文件size大于要下载的文件，说明这个文件是脏文件，删除
					deleteFile(tempFile);
					return existFile;
				} else if (existFile.length() > fileTotalSize) {
					existFile.delete();
				}
			}
			if (tempFile.exists() && tempFile.isFile()) {
				if (tempFile.length() == fileTotalSize) {
					deleteFile(existFile);
					File renameFile = createRenameFile(existFile.getAbsolutePath());
					tempFile.renameTo(renameFile);
					return renameFile;
				} else if (tempFile.length() > fileTotalSize) {
					// 当存在的文件size大于要下载的文件，说明这个文件是脏文件，删除
					tempFile.delete();
				}
			}
		}
		return null;
	}

	/**
	 * head 请求获取当前要下载文件的总长度
	 */
	private void getFileTotalLengthByHeadRequest() {
		FileRequest request = new FileRequest(fileUrl);
		request.mMethod = Request.RequestMethod.HEAD;
		HttpEntity headEntity = null;
		try {
			HttpResponse headResponse = httpWorker.doHttpRquest(request);
			int statusCode = headResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				fileTotalSize = getContentLength(headResponse);
			}

		} catch (IOException e) {
			e.printStackTrace();
			fileTotalSize = -1;
		} finally {
			if (headEntity != null) {
				try {
					headEntity.consumeContent();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	private long getContentLength(HttpResponse response) {
		Header[] headers = response.getHeaders("Content-Length");
		if (headers.length > 0) {
			return Long.valueOf(headers[0].getValue());
		}
		return 0;
	}

	/**
	 * 删除不为文件夹的文件
	 * 
	 * @param file
	 */
	private void deleteFile(File file) {
		if (file.exists() && file.isFile()) {
			file.delete();
		}
	}

	/**
	 * 创建下载文件存放文件夹
	 * 
	 * @param path
	 */
	private void initSaveFilePath(String path) {
		File tmpPath = new File(path);
		if (tmpPath.exists() && tmpPath.isDirectory()) {
			return;
		}
		tmpPath.mkdirs();
	}

	private void releaseConnection(InputStream in, RandomAccessFile raf) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取一个不存在的文件描述
	 * 
	 * @param filePath
	 * @return
	 */
	private static File createRenameFile(String filePath) {
		File file = new File(filePath);
		int i = 1;
		while (file.exists()) {
			file = new File(filePath + "_" + i);
			i++;
		}
		return file;
	}

}