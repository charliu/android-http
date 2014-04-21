package com.boyaa.texas.http;

/**
 * 
 * @author CharLiu
 *
 */
public abstract class Pojo {
	public Pojo() {}
	
	protected abstract Pojo parse(String str) throws Exception;
	
}
