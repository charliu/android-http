package com.vim.android;

import com.vim.ahttp.Pojo;

public class TestPojo extends Pojo {
	public String name;
	public int age;

	@Override
	protected Pojo parse(String str) throws Exception{
		TestPojo pojo = new TestPojo();
		pojo.name = "hello name";
		pojo.age = 123;
		return pojo;
	}

	@Override
	public String toString() {
		return "TestPojo [name=" + name + ", age=" + age + "]";
	}

}
