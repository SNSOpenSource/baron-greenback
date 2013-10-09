package com.googlecode.barongreenback.crawler.executor;

import java.util.Comparator;

import com.googlecode.barongreenback.crawler.jobs.JobComparator;
import com.googlecode.barongreenback.crawler.jobs.Job;

public class PriorityJobRunnable implements Runnable, Comparable<PriorityJobRunnable> {
	
	private static final Comparator<Job> COMPARATOR = JobComparator.masterJobsThenNewest();
	private final Job job;
	private final Runnable decorated;

	public PriorityJobRunnable(Job job, Runnable decorated) {
		this.job = job;
		this.decorated = decorated;
	}

	@Override
	public void run() {
		decorated.run();
	}

	@Override 
    public int compareTo(PriorityJobRunnable o) {
	    return COMPARATOR.compare(job, o.job);
    }

}
