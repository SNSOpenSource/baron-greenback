package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.RequestBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.jobs.HttpScheduler.INITIAL_DELAY;
import static com.googlecode.barongreenback.jobs.HttpScheduler.INTERVAL;
import static com.googlecode.barongreenback.jobs.HttpScheduler.JOB_ID;
import static com.googlecode.barongreenback.jobs.HttpScheduler.REQUEST;
import static com.googlecode.barongreenback.jobs.HttpScheduler.TIME_UNIT;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static org.junit.Assert.assertThat;

public class HttpSchedulerTest {
    private String request = RequestBuilder.get("/test").build().toString();
    private Record schedulerSpec = record().set(REQUEST, request).set(JOB_ID, "orders").set(INITIAL_DELAY, 0L).set(INTERVAL, 10L).set(TIME_UNIT, TimeUnit.SECONDS);
    private final StubFixedScheduler stub = new StubFixedScheduler();
    private final HttpScheduler httpScheduler = new HttpScheduler(stub, null);

    @Test
    public void scheduleRequest() throws Exception {
        String id = httpScheduler.schedule(schedulerSpec);

        assertThat(httpScheduler.jobs().size(), is(1));
        assertThat(httpScheduler.job(id).get().get(INTERVAL), CoreMatchers.is(10L));
        assertThat(stub.initialDelay, CoreMatchers.is(0L));
        assertThat(stub.delay, CoreMatchers.is(10L));
        assertThat(stub.unit, CoreMatchers.is(TimeUnit.SECONDS));
    }

    @Test
    public void rescheduleRequest() throws Exception {
        String id = httpScheduler.schedule(schedulerSpec);

        assertThat(stub.initialDelay, CoreMatchers.is(0L));
        assertThat(stub.delay, CoreMatchers.is(10L));
        assertThat(stub.unit, CoreMatchers.is(TimeUnit.SECONDS));


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

    private static class StubFixedScheduler implements FixedScheduler {
        public long initialDelay;
        public long delay;
        public TimeUnit unit;

        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, final long delay, TimeUnit unit) {
            this.initialDelay = initialDelay;
            this.delay = delay;
            this.unit = unit;
            return new DougDoesNotLikeTests(command);
        }
    }

    private static class DougDoesNotLikeTests<T> extends FutureTask<T> implements ScheduledFuture<T> {
        public DougDoesNotLikeTests(Runnable command) {
            super(command, null);
        }

        public long getDelay(TimeUnit unit) {
            return 0;
        }

        public int compareTo(Delayed o) {
            return 0;
        }
    }

}