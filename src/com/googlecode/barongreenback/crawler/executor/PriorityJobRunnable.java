package com.googlecode.barongreenback.crawler.executor;

import java.util.Comparator;

import com.googlecode.barongreenback.crawler.StagedJob;
import com.googlecode.barongreenback.crawler.StagedJobComparator;

public class PriorityJobRunnable implements Runnable, Comparable<PriorityJobRunnable> {
	
	private static final Comparator<StagedJob> COMPARATOR = StagedJobComparator.masterJobsThenNewest();
	private final StagedJob stagedJob;
	private final Runnable decorated;

	public PriorityJobRunnable(StagedJob stagedJob, Runnable decorated) {
		this.stagedJob = stagedJob;
		this.decorated = decorated;
	}

	@Override
	public void run() {
		decorated.run();
	}

	@Override 
    public int compareTo(PriorityJobRunnable o) {
	    return COMPARATOR.compare(stagedJob, o.stagedJob);
    }

}
