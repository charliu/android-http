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

import android.os.Environment;
import android.util.Log;

/**
 * 文件下载，支持断点续传，默认存放路径为/sdcard/tmp
 * 
 * @author CharLiu
 *
 */
public class FileDownloader {
	private static final String DEFAULT_FILE_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator + "tmp";

	public static DownloadTask download(final String fileUrl) {
		DownloadTask task = new DownloadTask(fileUrl);
		task.startDownload();
		return task;
	}

	public static class DownloadTask {
		private boolean stop = false;
		private final String fileUrl;
		private Thread downloadThread;
		private long fileTotalSize = -1;
		private HttpWorker httpWorker;
		private String fileName;

		DownloadTask(String url) {
			this.fileUrl = url;
			httpWorker = HttpWorkerFactory.createHttpWorker();
			fileName = getFileName(url);
		}

		public void startDownload() {
			downloadThread = new Thread(new Runnable() {
				@Override
				public void run() {
					download();
				}
			});
			downloadThread.start();
		}

		public void stopDownload() {
			this.stop = true;
		}

		public boolean isStoped() {
			return stop;
		}

		private String getFileName(String fileUrl) {
			return fileUrl.substring(fileUrl.lastIndexOf(File.separator));
		}

		private String getSaveFilePath(String fileName) {
			return DEFAULT_FILE_SAVE_PATH + File.separator + fileName;
		}

		/**
		 * 判断文件是否已下载
		 * 
		 * @return
		 */
		private boolean alreadyDownload() {
			File existFile = new File(getSaveFilePath(fileName));
			if (existFile.exists() && existFile.isFile()) {
				long tempFileSize = existFile.length();
				if (tempFileSize == 0)
					return false;
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
				} finally {
					if (headEntity != null) {
						try {
							headEntity.consumeContent();
						} catch (IOException e) {
							//
						}
					}

				}
				if (fileTotalSize != -1 && (fileTotalSize == tempFileSize)) {
					Log.i(Constants.HTTP_TAG, "file already downloaded");
					return true;
				}

			}
			return false;
		}

		private void download() {

			String fileTempName = CacheKeyUtil.generate(fileUrl);
			if (alreadyDownload()) {
				return;
			}
			initSaveFilePath(DEFAULT_FILE_SAVE_PATH);
			String fileTempPath = getSaveFilePath(fileTempName);
			File downloadTempFile = new File(fileTempPath);
			Map<String, String> header = new HashMap<String, String>();
			long downloadedSize = 0;
			if (downloadTempFile.exists()) {
				downloadedSize = downloadTempFile.length();
				header.put("Range", "bytes=" + downloadedSize + "-");
				Log.i(Constants.HTTP_TAG, "Http Range start with:" + downloadedSize);
			}
			FileRequest request = new FileRequest(fileUrl, header);

			InputStream in = null;
			RandomAccessFile raf = null;
			long readSize = 0;
			boolean deleteTempFile = false;
			try {
				HttpResponse response = httpWorker.doHttpRquest(request);
				int statusCode = response.getStatusLine().getStatusCode();
				Log.i(Constants.HTTP_TAG, "http statusCode:" + statusCode);

				if (fileTotalSize == -1) {
					fileTotalSize = getContentLength(response) + downloadedSize;
				}
				if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
					stop = false;
					raf = new RandomAccessFile(downloadTempFile, "rw");
					raf.seek(raf.length());
					in = response.getEntity().getContent();
					byte[] buffer = new byte[1024];
					long kbSize = 0;
					int length = 0;
					while (!stop && (length = in.read(buffer)) != -1) {
						readSize += length;
						long kb = readSize / 102400;
						if (kb > kbSize)
							Log.i(Constants.HTTP_TAG, "read size:" + kb);
						kbSize = kb;
						raf.write(buffer, 0, length);
					}
				} else if (statusCode == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
					deleteTempFile = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				stop = true;
				releaseConnection(in, raf);
			}
			if (deleteTempFile) {
				if (downloadTempFile.exists()) {
					downloadTempFile.delete();
				}
			}
			if (downloadTempFile.exists()) {
				if ((readSize + downloadedSize) == fileTotalSize) {
					File renameFile = createRenameFile(getSaveFilePath(fileName));
					if (!downloadTempFile.renameTo(renameFile)) {
						Log.e(Constants.HTTP_TAG, "Rename file failed, name:" + renameFile.getAbsolutePath());
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

	/**
	 * 创建下载文件存放文件夹
	 * 
	 * @param path
	 */
	private static void initSaveFilePath(String path) {
		File tmpPath = new File(path);
		if (tmpPath.exists() && tmpPath.isDirectory()) {
			return;
		}
		tmpPath.mkdirs();
	}

	private static void releaseConnection(InputStream in, RandomAccessFile raf) {
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

}
