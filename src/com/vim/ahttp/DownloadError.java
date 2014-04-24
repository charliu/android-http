package com.vim.ahttp;

@SuppressWarnings("serial")
public class DownloadError extends Error {
	private long fileTotalSize;
	private long downloadedSize;
	
	public DownloadError(Throwable t, int errorCode) {
		super(t, errorCode);
	}

	public DownloadError(int code) {
		super(code);
	}

	public long getFileTotalSize() {
		return fileTotalSize;
	}

	public void setFileTotalSize(long fileTotalSize) {
		this.fileTotalSize = fileTotalSize;
	}

	public long getDownloadedSize() {
		return downloadedSize;
	}

	public void setDownloadedSize(long downloadedSize) {
		this.downloadedSize = downloadedSize;
	}
}
