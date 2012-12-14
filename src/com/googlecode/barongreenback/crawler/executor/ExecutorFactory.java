package com.googlecode.barongreenback.crawler.executor;

public interface ExecutorFactory {

	public abstract ThreadPoolJobExecutor jobExecutor(int threads,
	        int capacity, String name);

}