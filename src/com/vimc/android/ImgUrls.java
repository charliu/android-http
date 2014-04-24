package com.vimc.android;

import java.util.ArrayList;
import java.util.List;

public class ImgUrls {
	public static List<String> getBig(int size) {
		List<String> list = new ArrayList<String>();
		String baseUrl = "http://pic1.sc.chinaz.com/files/pic/pic9/201404/apic";
		for (int i = 110; i < 110 + size; i++) {
			list.add(baseUrl + i + ".jpg");
		}
		return list;
	}
	
	public static List<String> getSmall(int size) {
		List<String> list = new ArrayList<String>();
		String baseUrl = "http://cdn-img.easyicon.net/png/11414/";
		for (int i = 1141416; i < 1141416 + size; i++) {
			list.add(baseUrl + i + ".gif");
		}
		return list;
	}
}
