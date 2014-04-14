package com.boyaa.texas.http;

/**
 * 
 * @author charliu
 *
 * @param <T> 缓存对象
 */
public interface Cache<T> {
	void init();
	
	void put(String cacheKey, T value);

	T get(String cacheKey);
}
