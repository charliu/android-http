package com.vimc.ahttp;

import com.vimc.ahttp.FileDownloadTask.DownloadListener;

/**
 * 
 * @author CharLiu
 * 
 */
public class FileDownloader {

	public static FileDownloadTask download(final String fileUrl, String savePath, DownloadListener listener) {
		FileDownloadTask task = new FileDownloadTask(fileUrl, savePath, listener);
		task.startDownload();
		return task;
	}

}
