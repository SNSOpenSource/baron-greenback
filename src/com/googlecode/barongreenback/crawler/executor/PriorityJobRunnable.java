package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.jobs.Job;
import com.googlecode.barongreenback.crawler.jobs.JobComparator;

import java.util.Comparator;
import java.util.UUID;

public class PriorityJobRunnable implements Runnable, Comparable<PriorityJobRunnable> {
	
	private static final Comparator<Job> COMPARATOR = JobComparator.masterJobsThenNewest();
	private final Job job;
	private final Runnable decorated;
	private UUID id = UUID.randomUUID();

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

	public UUID id() {
		return id;
	}

}
