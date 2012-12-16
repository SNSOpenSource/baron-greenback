package com.googlecode.barongreenback.crawler.executor;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.barongreenback.crawler.HttpJob;
import com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Pair;

public class ThreadPoolExecutorFactoryTest {

	private CountDownLatch overallCountDownLatch;
	
	private Semaphore setupComplete = new Semaphore(1);

	private Integer inputHandlerThreads = 1;
	
	private int nonMasterProcessedCount = 0;

	private final int numNonMasterJobs = 10;
	
	private CountDownLatch subsequentNonMasterJobsLatch = new CountDownLatch(1);
	
	private final int inputHandlerCapacity = numNonMasterJobs + 1;
	
	@Test
	public void masterJobsJumpQueue() throws Exception {
	    overallCountDownLatch = new CountDownLatch(numNonMasterJobs);
	    
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

    private void cleanUp() throws InterruptedException {
        releaseFirstQueueItem();
        allowAllJobsToRun();
		waitForQueueToDrain();
    }
	
	@Test
	public void surplusMessagesCauseErrors() throws Exception {
	    int totalJobs = numNonMasterJobs + numOfAdditionalJobsNeededToExceedCapacity();
	    overallCountDownLatch = new CountDownLatch(totalJobs);
	    
	    JobExecutor jobExecutor = getJobExecutor();
		
		stopQueueFromProcessing();
		
		putNonMasterJobsOnQueue(jobExecutor);
		
		CountDownLatch excessLatch = exceedQueueCapacityInAnotherThread(jobExecutor);
		
		assertFalse(isThreadAddingExcessJobsFinished(excessLatch));
		
		cleanUp();
        assertThat(getNumberOfProcessedNonMasterJobs(), CoreMatchers.is(totalJobs));
        assertTrue(isThreadAddingExcessJobsFinished(excessLatch));
	}

    private boolean isThreadAddingExcessJobsFinished(CountDownLatch excessLatch)
            throws InterruptedException {
        return excessLatch.await(2, TimeUnit.SECONDS);
    }

    private CountDownLatch exceedQueueCapacityInAnotherThread(final JobExecutor jobExecutor) {
        int numExcessJobs = numOfAdditionalJobsNeededToExceedCapacity();
        final CountDownLatch excessLatch = new CountDownLatch(numExcessJobs);
        Thread producerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < numOfAdditionalJobsNeededToExceedCapacity() ; i++) {
                    putANonMasterJobOnQueue(jobExecutor);
                    excessLatch.countDown();
                }
            }
        });
        producerThread.start();
        return excessLatch;
    }

    
    private int numOfAdditionalJobsNeededToExceedCapacity() {
        int excess = inputHandlerCapacity - numNonMasterJobs + 2; // 2 = 1 to exceed limit + 1 because the single thread of the executor has removed an element
        return excess > 0 ? excess : 0;
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
	    	putANonMasterJobOnQueue(jobExecutor);
		}
    }

    private void putANonMasterJobOnQueue(JobExecutor jobExecutor) {
        HttpJob stagedJob = createHttpJob();
        PriorityJobRunnable command 	= new BlockingPriorityJobRunnable(stagedJob);
        jobExecutor.execute(command);
    }

	private HttpJob createHttpJob() {
	    HashSet<Pair<String, Object>> values = new HashSet<Pair<String,Object>>();
	    values.add(Pair.pair("createdDate", (Object)(new Date())));
	    Model context = Model.immutable.model(values);
	    HttpJob 			stagedJob 	= HttpJob.httpJob(context );
	    return stagedJob;
    }
	
	private JobExecutor getJobExecutor() {
		JobExecutor jobExecutor = new ThreadPoolExecutorFactory().jobExecutor(inputHandlerThreads, inputHandlerCapacity, "test name");
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
		public BlockingPriorityJobRunnable(HttpJob stagedJob) {
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
	

}
