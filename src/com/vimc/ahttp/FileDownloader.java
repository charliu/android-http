package com.vimc.ahttp;

import java.io.File;

import com.vimc.ahttp.FileDownloadTask.DownloadListener;

import android.os.Environment;

/**
 * 文件下载，支持断点续传，默认存放路径为/sdcard/tmp
 * 
 * @author CharLiu
 * 
 */
public class FileDownloader {
	public static final String DEFAULT_FILE_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator + "tmp";

	public static FileDownloadTask download(final String fileUrl) {
		return download(fileUrl, DEFAULT_FILE_SAVE_PATH, null, null);
	}
	
	public static FileDownloadTask download(final String fileUrl, DownloadListener listener) {
		return download(fileUrl, DEFAULT_FILE_SAVE_PATH, null, listener);
	}

	public static FileDownloadTask download(final String fileUrl, String savePath) {
		return download(fileUrl, savePath, null, null);
	}

	public static FileDownloadTask download(final String fileUrl, String savePath, String saveName,
			DownloadListener listener) {
		FileDownloadTask task = new FileDownloadTask(fileUrl, DEFAULT_FILE_SAVE_PATH, listener);
		task.startDownload();
		return task;
	}

}
