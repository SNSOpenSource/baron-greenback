package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;

import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerLatch {

    private AtomicInteger count = new AtomicInteger();

    public int increment(int i) {
        int i1 = count.addAndGet(i);
        System.out.println("i1 = " + i1);
        return i1;
    }

    public synchronized void decrement() {
        int c = count.decrementAndGet();
        System.out.println("c; = " + c);
        if (c == 0) {
            this.notify();
        }
    }

    public synchronized void await() throws InterruptedException {
        this.wait();
        System.out.println("count = " + count);
    }

    public static <R> Function<R> countUp(final CrawlerLatch latch, final Function<R> f) {
        return new Function<R>() {
            @Override
            public R call() throws Exception {
                int increment = latch.increment(1);
                System.out.println("increment = " + increment);
                R r = f.call();
                latch.decrement();
                return r;
            };
        };
    }

//    Function1<Response, Sequence<Record>>

    public static <T, R> Function1<T,R> countUp(final CrawlerLatch latch, final Function1<T, R> f) {
        return new Function1<T, R>() {
            @Override
            public R call(T t) throws Exception {
                int increment = latch.increment(1);
                System.out.println("increment = " + increment);
                R r = f.call(t);
                latch.decrement();
                return r;
            }
        };
    }
}
