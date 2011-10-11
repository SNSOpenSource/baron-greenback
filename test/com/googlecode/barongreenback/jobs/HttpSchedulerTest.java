package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.memory.MemoryRecords;
import com.googlecode.utterlyidle.RequestBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.jobs.HttpScheduler.SECONDS;
import static com.googlecode.barongreenback.jobs.HttpScheduler.JOB_ID;
import static com.googlecode.barongreenback.jobs.HttpScheduler.REQUEST;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static org.junit.Assert.assertThat;

public class HttpSchedulerTest {
    private String request = RequestBuilder.get("/test").build().toString();
    private Record schedulerSpec = record().set(REQUEST, request).set(JOB_ID, UUID.randomUUID()).set(SECONDS, 10L);
    private final StubScheduler stub = new StubScheduler();
    private final HttpScheduler httpScheduler = new HttpScheduler(new MemoryRecords(), stub, null);

    @Test
    public void scheduleRequest() throws Exception {
        UUID id = httpScheduler.schedule(schedulerSpec);

        assertThat(httpScheduler.jobs().size(), is(1));
        assertThat(httpScheduler.job(id).get().get(SECONDS), CoreMatchers.is(10L));
        assertThat(stub.delay, CoreMatchers.is(10L));
    }

    @Test
    public void rescheduleRequest() throws Exception {
        UUID id = httpScheduler.schedule(schedulerSpec);
        assertThat(stub.delay, CoreMatchers.is(10L));

        httpScheduler.schedule(schedulerSpec.set(SECONDS, 20L));

        assertThat(httpScheduler.jobs().size(), is(1));
        assertThat(httpScheduler.job(id).get().get(SECONDS), CoreMatchers.is(20L));
        assertThat(stub.delay, CoreMatchers.is(20L));
    }

    @Test
    public void removeScheduledJob() throws Exception {
        UUID id = httpScheduler.schedule(schedulerSpec);
        httpScheduler.remove(id);
        assertThat(httpScheduler.jobs().size(), is(0));
    }

    private static class StubScheduler implements Scheduler {
        public long delay;

        public Job schedule(UUID id, Callable<?> command, final long numberOfSeconds) {
            this.delay = numberOfSeconds;
            return doNothingJob();
        }

        public void cancel(UUID id) {
        }

        private Job doNothingJob() {
            return new Job() {
                public void cancel() {
                }
            };
        }
    }

}