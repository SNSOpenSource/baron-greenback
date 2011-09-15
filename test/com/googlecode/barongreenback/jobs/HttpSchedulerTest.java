package com.googlecode.barongreenback.jobs;

import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.RequestBuilder;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.jobs.HttpScheduler.INITIAL_DELAY;
import static com.googlecode.barongreenback.jobs.HttpScheduler.INTERVAL;
import static com.googlecode.barongreenback.jobs.HttpScheduler.TIME_UNIT;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HttpSchedulerTest {
    private ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
    private Application application = mock(Application.class);
    private HttpScheduler httpScheduler = new HttpScheduler(executorService, application);
    private ScheduledFuture future = mock(ScheduledFuture.class);

    @Test
    public void scheduleRequest() throws Exception {
        UUID id = httpScheduler.schedule(RequestBuilder.get("/test").build(), record().set(INITIAL_DELAY, 0L).set(INTERVAL, 10L).set(TIME_UNIT, TimeUnit.SECONDS));

        assertThat(httpScheduler.jobs().size(), is(1));
        assertThat(httpScheduler.job(id).get().get(INTERVAL), is(10L));
        verify(executorService).scheduleWithFixedDelay(Mockito.any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void rescheduleRequest() throws Exception {
        Mockito.when(executorService.scheduleWithFixedDelay(Mockito.any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(future);
        UUID id = httpScheduler.schedule(RequestBuilder.get("/test").build(), record().set(INITIAL_DELAY, 0L).set(INTERVAL, 10L).set(TIME_UNIT, TimeUnit.SECONDS));

        httpScheduler.reschedule(id, record().set(INTERVAL, 20L));

        assertThat(httpScheduler.jobs().size(), is(1));
        assertThat(httpScheduler.job(id).get().get(INTERVAL), is(20L));

        InOrder inOrder = inOrder(executorService);
        inOrder.verify(executorService).scheduleWithFixedDelay(Mockito.any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
        inOrder.verify(executorService).scheduleWithFixedDelay(Mockito.any(Runnable.class), eq(0L), eq(20L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void removeScheduledJob() throws Exception {
        Mockito.when(executorService.scheduleWithFixedDelay(Mockito.any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(future);
        UUID id = httpScheduler.schedule(RequestBuilder.get("/test").build(), record().set(INITIAL_DELAY, 0L).set(INTERVAL, 10L).set(TIME_UNIT, TimeUnit.SECONDS));

        httpScheduler.remove(id);

        assertThat(httpScheduler.jobs().size(), is(0));
    }

}
