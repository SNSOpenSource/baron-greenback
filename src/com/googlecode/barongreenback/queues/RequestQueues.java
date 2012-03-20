package com.googlecode.barongreenback.queues;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.Sequences.sequence;

public class RequestQueues implements Queues {
    private final LinkedBlockingQueue<CompletedJob> completed = new CappedLinkedBlockingQueue<CompletedJob>(20);
    public static class CappedLinkedBlockingQueue<T> extends LinkedBlockingQueue<T> {
        public CappedLinkedBlockingQueue(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public boolean add(T t) {
            if (remainingCapacity() == 0) {
                try {
                    take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return super.add(t);
        }
    }

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
        completer.complete(handle(request));
    }

    private Callable<Void> handle(final Request request) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                complete(request);
                return VOID;
            }
        };
    }

    private void complete(Request request) throws Exception {
        Date started = clock.now();
        Response response = application.handle(request);
        Date completedDate = clock.now();
        completed.add(new CompletedJob(request, response, started, completedDate));
    }

}
