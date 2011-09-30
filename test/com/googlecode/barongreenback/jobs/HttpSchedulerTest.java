package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.memory.MemoryRecords;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.jobs.HttpScheduler.INTERVAL;
import static com.googlecode.barongreenback.jobs.HttpScheduler.JOB_ID;
import static com.googlecode.barongreenback.jobs.HttpScheduler.REQUEST;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static org.junit.Assert.assertThat;

public class HttpSchedulerTest {
    private String request = RequestBuilder.get("/test").build().toString();
    private Record schedulerSpec = record().set(REQUEST, request).set(JOB_ID, "orders").set(INTERVAL, 10L);
    private final StubScheduler stub = new StubScheduler();
    private final HttpScheduler httpScheduler = new HttpScheduler(new MemoryRecords(), stub, null);

    @Test
    public void scheduleRequest() throws Exception {
        String id = httpScheduler.schedule(schedulerSpec);

        assertThat(httpScheduler.jobs().size(), is(1));
        assertThat(httpScheduler.job(id).get().get(INTERVAL), CoreMatchers.is(10L));
        assertThat(stub.delay, CoreMatchers.is(10L));
    }

    @Test
    public void rescheduleRequest() throws Exception {
        String id = httpScheduler.schedule(schedulerSpec);
        assertThat(stub.delay, CoreMatchers.is(10L));

        httpScheduler.schedule(schedulerSpec.set(INTERVAL, 20L));

        assertThat(httpScheduler.jobs().size(), is(1));
        assertThat(httpScheduler.job(id).get().get(INTERVAL), CoreMatchers.is(20L));
        assertThat(stub.delay, CoreMatchers.is(20L));
    }

    @Test
    public void removeScheduledJob() throws Exception {
        String id = httpScheduler.schedule(schedulerSpec);
        httpScheduler.remove(id);
        assertThat(httpScheduler.jobs().size(), is(0));
    }

    private static class StubScheduler implements Scheduler {
        public long delay;

        public Job schedule(String id, Callable<Response> command, final long delay) {
            this.delay = delay;
            return doNothingJob();
        }

        public void cancel(String id) {
        }

        private Job doNothingJob() {
            return new Job() {
                public void cancel() {
                }
            };
        }
    }

}