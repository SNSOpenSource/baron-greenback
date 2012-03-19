package com.googlecode.barongreenback.queues;

import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static org.hamcrest.MatcherAssert.assertThat;

public class QueuesResourceTest extends ApplicationTests {
    @Test
    public void canQueueARequest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        application.applicationScope().addInstance(CountDownLatch.class, latch).
                decorate(Completer.class, CountDownCompleter.class);

        QueuesPage queues = new QueuesPage(browser);
        assertThat(queues.numberOfCompletedJobs(), is(0));

        Response response = queues.queue(post("some/url").build());

        assertThat(response.status(), is(Status.ACCEPTED));
        latch.await();
        queues = new QueuesPage(browser);
        assertThat(queues.numberOfCompletedJobs(), is(1));
    }
}