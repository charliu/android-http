package com.vimc.ahttp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.text.TextUtils;

/**
 * Bitmap SdCard缓存
 * @author CharLiu
 *
 */
public class ImageDiskCache extends Cache<Bitmap> {
	File cacheDir;
	Options options = ImageLoader.getDefaultOptions();
	
	/**
	 * 
	 * @param cacheDir 缓存目录
	 */
	public ImageDiskCache(File cacheDir) {
		if (cacheDir == null) {
			throw new IllegalArgumentException("Cache dir can't be null");
		}
		this.cacheDir = cacheDir;
	}

	@Override
	void put(String cacheKey, Bitmap value) {
		if (TextUtils.isEmpty(cacheKey) || value == null) {
			return;
		}
		File file = new File(cacheDir, cacheKey);
		if (file.exists()) {
			HLog.w("Image file is alerday in sdcard, FileName:" + file.getName());
			return;
		}
		File parentFile = file.getParentFile();
		if (!parentFile.exists()) {
			if (!parentFile.mkdirs()) {
				HLog.e("Image disk cache dir not available");
				return;
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			value.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	Bitmap get(String cacheKey) {
		File file = new File(cacheDir, cacheKey);
		if (file.exists()) {
			FileInputStream in = null;
			Bitmap bitmap = null;
			try {
				in = new FileInputStream(file);
				bitmap = BitmapFactory.decodeStream(in, null, options);
			} catch (FileNotFoundException notFoundError) {
				if (HLog.Config.LOG_E) notFoundError.printStackTrace();
			} catch (OutOfMemoryError outMemeoryError) {
				if (HLog.Config.LOG_E) outMemeoryError.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return bitmap;
		}
		return null;
	}

	@Override
	void clear() {
		if (cacheDir != null && cacheDir.isDirectory()) {
			File[] files = cacheDir.listFiles();
			if (files != null) {
				for (File f : files) {
					f.delete();
				}
			}
		}
	}

}
