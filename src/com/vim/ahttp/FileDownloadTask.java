package com.vim.ahttp;

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

/**
 * 文件下载任务 {@link #startDownload() 开始下载 } {@link #stopDownload() 停止下载 }
 * 
 * @author CharLiu
 * 
 */
public class FileDownloadTask {
	private final String fileUrl; // 文件url地址
	private final String tempFileName; // 临时文件名称，为fileUrl的md5值
	private final String fileSavePath; // 文件保存路径
	private final HttpWorker httpWorker;
	private final DownloadListener downloadListener;
	private Handler postHandler;

	private Thread downloadThread;
	private long fileTotalSize = -1;
	private long downloadedTempFileSize = 0; // 已下载部分字节数
	private long readSize = 0; // 当前进程下载所读取的字节数
	private int currentPercent = 0; // 下载百分比
	private volatile boolean stop = false;
	private String fileName;

	/**
	 * @param url
	 *            文件URL地址
	 * @param savePath
	 *            保存路径
	 * @param listener
	 *            下载过程回调函数，可以为null
	 */
	FileDownloadTask(String url, String savePath, DownloadListener listener) {
		this.fileUrl = url;
		this.fileSavePath = savePath;
		httpWorker = HttpWorkerFactory.createHttpWorker();
		fileName = getFileName(url);
		tempFileName = MD5Util.generateMD5(fileUrl);
		downloadListener = listener;
		if (downloadListener != null) {
			postHandler = new Handler(Looper.getMainLooper());
		}
	}

	/**
	 * 下载回调接口
	 * 
	 * @author CharLiu {@link #onStart(String) 开始下载}
	 *         {@link #onUpdateProgress(long, long, int) 更新进度}
	 *         {@link #onError(DownloadError) 下载出错} {@link #onComplete(File)
	 *         下载完成}
	 */
	public interface DownloadListener {
		void onStart(String fileUrl);

		void onUpdateProgress(long currentSize, long totalSize, int percent);

		void onComplete(File downloadedFile);

		void onError(DownloadError error);
	}

	/**
	 * 开始下载
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
		}, "FileDownload thead");

		downloadThread.start();
	}
	
	/**
	 * 停止下载，停止后可以调用startDownload继续下载
	 */
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

	private String getSaveFileAbsolutePath(String fileName) {
		return fileSavePath + File.separator + fileName;
	}

	private Map<String, String> getRangeHeaderByFile(File file) {
		Map<String, String> header = new HashMap<String, String>();
		if (file.exists()) {
			long downloadStartWith = file.length();
			downloadedTempFileSize = downloadStartWith;
			header.put("Range", "bytes=" + downloadStartWith + "-");
			HLog.i("Http Range start with:" + downloadStartWith);
		}
		return header;
	}

	/**
	 * 下载文件
	 */
	private void download() {
		File existFile = checkFileExists();
		if (existFile != null && existFile.exists()) {
			HLog.i("File already download");
			postDonwloadSuccess(existFile);
			return;
		}

		resetSize();

		initSaveFilePath(fileSavePath);
		String fileTempPath = getSaveFileAbsolutePath(tempFileName);
		File downloadTempFile = new File(fileTempPath);
		Map<String, String> headers = getRangeHeaderByFile(downloadTempFile);
		FileRequest request = new FileRequest(fileUrl, headers);

		HttpEntity entity = null;
		RandomAccessFile raf = null;
		boolean badTempFile = false;
		try {
			HttpResponse response = httpWorker.doHttpRquest(request);
			int statusCode = response.getStatusLine().getStatusCode();
			HLog.i("http statusCode:" + statusCode);

			if (fileTotalSize == -1 && headers.size() == 0) {
				fileTotalSize = getContentLength(response);
			}
			HLog.i("fileTotalSize:" + fileTotalSize);
			if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
				if (response.getEntity() != null) {
					raf = new RandomAccessFile(downloadTempFile, "rw");
					raf.seek(raf.length());
					InputStream inputStream = response.getEntity().getContent();
					byte[] buffer = new byte[1024];
					int length = 0;
					int percent = 0;
					stop = false;
					while (!stop && (length = inputStream.read(buffer)) != -1) {
						raf.write(buffer, 0, length);
						readSize += length;
						long downloadedTotalSize = downloadedTempFileSize + readSize;
						currentPercent = (int) ((downloadedTotalSize * 100) / fileTotalSize);
						if (currentPercent > percent) {
							HLog.i("read size:" + downloadedTotalSize + " read percent:" + currentPercent);
							postUpdateProgress();
						}
						percent = currentPercent;
					}
				} else {
					throw new IOException("Entity is null");
				}

			} else if (statusCode == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
				badTempFile = true;
				throw new IOException("Requested range not satisfiable");
			} else {
				throw new IOException("Server error,statusCode not 200 or 206");
			}

		} catch (IOException e) {
			stop = true;
			postError(e);
			e.printStackTrace();
		} finally {
			stop = true;
			releaseResouce(entity, raf);
		}
		if (badTempFile) {
			if (downloadTempFile.exists()) {
				downloadTempFile.delete();
			}
		}
		if (downloadTempFile.exists()) {
			if ((readSize + downloadedTempFileSize) == fileTotalSize) {
				final File renameFile = createRenameFile(getSaveFileAbsolutePath(fileName));
				if (downloadTempFile.renameTo(renameFile)) {
					postDonwloadSuccess(renameFile);
				} else {
					HLog.e("Rename file failed, name:" + renameFile.getAbsolutePath());
				}
			}
		}

	}

	/**
	 * 重置size
	 */
	private void resetSize() {
		readSize = 0;
		currentPercent = 0;
		downloadedTempFileSize = 0;
	}

	private void postError(final IOException e) {
		final DownloadError error = new DownloadError(e, Error.NETWORK_ERROR);
		error.setFileTotalSize(fileTotalSize);
		error.setDownloadedSize(readSize + downloadedTempFileSize);
		if (downloadListener != null) {
			postHandler.post(new Runnable() {
				@Override
				public void run() {
					downloadListener.onError(error);
				}
			});
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
					downloadListener.onUpdateProgress(readSize + downloadedTempFileSize, fileTotalSize, currentPercent);
				}
			});
		}
	}

	/**
	 * 判断文件是否已下载 根据要下载文件实际总长度(用http head请求或得)与本地临时文件或已存在的同名文件SIZE对比
	 * 如果相等则认为是已经下载过了
	 * 
	 * @return 如果已下载就返回这个file， 没有返回null
	 */
	private File checkFileExists() {
		File existFile = new File(getSaveFileAbsolutePath(fileName));
		File tempFile = new File(getSaveFileAbsolutePath(tempFileName));

		if ((existFile.exists() && existFile.isFile()) || (tempFile.exists() && tempFile.isFile())) {
			getFileTotalLengthByHeadRequest();
		} else {
			return null;
		}
		if (fileTotalSize != -1) {
			if (existFile.exists() && existFile.isFile()) {
				if (existFile.length() == fileTotalSize) {
					// 当存在的文件size大于要下载的文件，就认为这个文件是脏文件，删除
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
					// 当存在的文件size大于要下载的文件，就认为这个文件是脏文件，删除
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
			} else {
				fileTotalSize = -1;
			}

		} catch (IOException e) {
			fileTotalSize = -1;
			if (Constants.LOG_E) {
				e.printStackTrace();
			}
		} finally {
			if (headEntity != null) {
				try {
					headEntity.consumeContent();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}
	}

	private long getContentLength(HttpResponse response) {
		Header[] headers = response.getHeaders("Content-Length");
		if (headers.length > 0) {
			try {
				long length = Long.valueOf(headers[0].getValue());
				return length;
			} catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
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

	private void releaseResouce(HttpEntity entity, RandomAccessFile raf) {
		if (entity != null) {
			try {
				entity.consumeContent();
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