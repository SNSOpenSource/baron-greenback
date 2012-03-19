package com.googlecode.barongreenback.queues;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import static com.googlecode.totallylazy.Sequences.sequence;

public class RequestQueues implements Queues {
    private final LinkedBlockingQueue<CompletedJob> completed = new LinkedBlockingQueue<CompletedJob>();
    private final Application application;
    private final Clock clock;
    private final Completer completer;

    public RequestQueues(Application application, Clock clock, Completer completer) {
        this.application = application;
        this.clock = clock;
        this.completer = completer;
    }

    @Override
    public Sequence<CompletedJob> completed() {
        return sequence(completed);
    }

    @Override
    public void queue(final Request request) {
        completer.complete(handle(request), add(completed));
    }

    public static <T> Callable1<T, Boolean> add(final Collection<T> collection) {
        return new Callable1<T, Boolean>() {
            @Override
            public Boolean call(T instance) throws Exception {
                return collection.add(instance);
            }
        };
    }

    private Callable<CompletedJob> handle(final Request request) {
        return new Callable<CompletedJob>() {
            @Override
            public CompletedJob call() throws Exception {
                return complete(request);
            }
        };
    }

    private CompletedJob complete(Request request) throws Exception {
        Date started = clock.now();
        Response response = application.handle(request);
        Date completed = clock.now();
        return new CompletedJob(request, response, started, completed);
    }
}
