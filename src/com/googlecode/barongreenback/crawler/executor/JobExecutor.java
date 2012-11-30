package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.StatusMonitor;

import java.io.Closeable;
import java.util.concurrent.Executor;

public interface JobExecutor extends StatusMonitor, Closeable {

	public void execute(PriorityJobRunnable command);
	
}
