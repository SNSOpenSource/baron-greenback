package com.googlecode.barongreenback.crawler.executor;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.barongreenback.crawler.CrawlerHttpClient;
import com.googlecode.barongreenback.crawler.HttpDatasource;
import com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob;
import com.googlecode.barongreenback.crawler.StagedJob;
import com.googlecode.barongreenback.crawler.StagedJobExecutor;
import com.googlecode.barongreenback.crawler.failures.FailureHandler;
import com.googlecode.barongreenback.crawler.failures.FailureRepository;
import com.googlecode.barongreenback.crawler.failures.Failures;
import com.googlecode.funclate.Model;
import com.googlecode.funclate.ModelFactory;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.CountLatch;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;

public class CrawlerExecutorsTest {

	private CrawlerExecutors crawlerExecutors;
	
	private CountDownLatch overallCountDownLatch;
	
	private Semaphore setupComplete = new Semaphore(1);

	private Integer inputHandlerThreads = 1;
	
	private int nonMasterProcessedCount = 0;

	private final int numNonMasterJobs = 10;
	
	private CountDownLatch subsequentNonMasterJobsLatch = new CountDownLatch(1);
	
	private final int inputHandlerCapacity = numNonMasterJobs + 1;

	@Before
	public void setUp() {
		Integer processHandlerThreads = 1;
		Integer processHandlerCapacity = 1;
		Integer outputHandlerThreads = 1;
		Integer outputHandlerCapacity = 1;
		Application application = null;
		crawlerExecutors = new CrawlerExecutors(
				inputHandlerThreads, 
				inputHandlerCapacity, 
				processHandlerThreads, 
				processHandlerCapacity, 
				outputHandlerThreads, 
				outputHandlerCapacity, 
				application);
	}
	
	@Before
	public void initLatch() {
		overallCountDownLatch = new CountDownLatch(numNonMasterJobs);
	}
	
	@After 
	public void tearDown() {
		if (crawlerExecutors != null) {
			crawlerExecutors.close();
		}
	}
	
	@Test
	public void masterJobsJumpQueue() throws Exception {
		JobExecutor jobExecutor = getJobExecutor();
		
		stopQueueFromProcessing();
		
		putNonMasterJobsOnQueue(jobExecutor);
		
		ListeningPriorityJobRunnable masterCommand 
			= putMasterJobOnQueue(jobExecutor);
		
		releaseFirstQueueItem();
		
		assertTrue(masterCommand.isProcessingFinished());
		
		allowAllJobsToRun();
		
		waitForQueueToDrain();
		
		assertThat(getNumberOfProcessedNonMasterJobs(), CoreMatchers.is(numNonMasterJobs));
		assertTrue(masterCommand.hasRun);
	}

	private void waitForQueueToDrain() throws InterruptedException {
	    assertTrue(overallCountDownLatch.await(1, TimeUnit.SECONDS));
    }

	private void allowAllJobsToRun() {
	    subsequentNonMasterJobsLatch.countDown();
    }

	private void releaseFirstQueueItem() {
	    setupComplete.release();
    }

	private void stopQueueFromProcessing() throws InterruptedException {
	    setupComplete.acquire();
    }

	private ListeningPriorityJobRunnable putMasterJobOnQueue(JobExecutor jobExecutor) {
	    Model 		model 		= Model.immutable.model();
		MasterPaginatedHttpJob masterJob = MasterPaginatedHttpJob.masterPaginatedHttpJob(model);
		ListeningPriorityJobRunnable masterCommand = new ListeningPriorityJobRunnable(masterJob);
		jobExecutor.execute(masterCommand);
	    return masterCommand;
    }

	private void putNonMasterJobsOnQueue(JobExecutor jobExecutor) {
	    for (int i = 0 ; i < numNonMasterJobs  ; i++) {
	    	NonMasterJob 		stagedJob 	= createJobWithDefinition();
	    	PriorityJobRunnable command 	= new BlockingPriorityJobRunnable(stagedJob);
			jobExecutor.execute(command);
		}
    }
	
	private JobExecutor getJobExecutor() {
		StagedJob 	discarded 	= createJobWithDefinition();
		JobExecutor jobExecutor = crawlerExecutors.inputHandler(discarded);
		return jobExecutor;
	}

	private class ListeningPriorityJobRunnable extends PriorityJobRunnable {
		
		private volatile boolean hasRun = false;
		
		private final CountDownLatch finishedProcessingLatch = new CountDownLatch(1);

		public ListeningPriorityJobRunnable(MasterPaginatedHttpJob stagedJob) {
			super(stagedJob, new NullRunnable());
		}

		@Override
		public void run() {
			super.run();
			hasRun = true;
			finishedProcessingLatch.countDown();
			System.out.println("Master finished. Number of non masters processed: " + getNumberOfProcessedNonMasterJobs());
		}
		
		public boolean hasRun() {
			return hasRun;
		}
		
		public boolean isProcessingFinished() throws InterruptedException {
			return finishedProcessingLatch.await(2, TimeUnit.SECONDS);
		}
	}
	
	private class BlockingPriorityJobRunnable extends PriorityJobRunnable {
		public BlockingPriorityJobRunnable(NonMasterJob stagedJob) {
			super(stagedJob, new NullRunnable());
		}

		@Override
		public void run() {
			blockUntilSetupComplete();
			blockIfNotFirstJob();
			super.run();
			System.out.println("Non master finished: I am number " + getNumberOfProcessedNonMasterJobs());
			incrementNumberOfProcessedNonMasterJobs();
			release();
		}

		private void blockIfNotFirstJob() {
	        if (notFirstNonMasterToRun()) {
				try {
	                subsequentNonMasterJobsLatch.await(100, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
	                throw new RuntimeException(e);
                }
			}
        }

		private boolean notFirstNonMasterToRun() {
	        return getNumberOfProcessedNonMasterJobs() != 0;
        }

		private void release() {
	        setupComplete.release();
        }

		private void blockUntilSetupComplete() {
	        try {
				setupComplete.acquire();
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
        }
	}
	
	private class NullRunnable implements Runnable {

		@Override
        public void run() { }
		
	}
	
	private synchronized void incrementNumberOfProcessedNonMasterJobs() {
		nonMasterProcessedCount += 1;
		overallCountDownLatch.countDown();
	}
	
	private synchronized int getNumberOfProcessedNonMasterJobs() {
		return nonMasterProcessedCount;
	}

	private NonMasterJob createJobWithDefinition() {
		return new NonMasterJob();
	}
	
	private class NonMasterJob implements StagedJob {

		@Override
		public Set<HttpDatasource> visited() { return null; }
		
		@Override
		public Record record() { return null; }
		
		@Override
		public Pair<Sequence<Record>, Sequence<StagedJob>> process(Container scope,
				Response response) throws Exception {  return null; }
		
		@Override
		public Definition destination() { return initDefinition(); }
		
		@Override
		public HttpDatasource datasource() { 
				Uri uri = new Uri("http", "authority", "path", "query", "fragment");
				return HttpDatasource.httpDatasource(uri, null);
		}
		
		@Override
		public UUID crawlerId() { return null; }

		@Override
		public Date createdDate() { return null; }
	}
	
	private Definition initDefinition() {
		return new Definition() {
			@Override
			public String name() { return null; }

			@Override
			public int compareTo(Definition o) { return 0; }

			@Override
			public Sequence<Keyword<?>> fields() { return null; }
		};
	}

}
