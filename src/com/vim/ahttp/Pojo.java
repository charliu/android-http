package com.vim.ahttp;

/**
 * 
 * @author CharLiu
 *
 */
public abstract class Pojo {
	public Pojo() {}
	
	protected abstract Pojo parse(String str) throws Exception;
	
}
