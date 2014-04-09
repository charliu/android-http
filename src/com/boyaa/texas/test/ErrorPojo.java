package com.boyaa.texas.test;

import com.boyaa.texas.http.Pojo;

public class ErrorPojo extends Pojo{

	@Override
	protected Pojo parse(String str) {
		return new ErrorPojo();
	}

}
