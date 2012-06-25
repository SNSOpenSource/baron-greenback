package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import static com.googlecode.totallylazy.Sequences.sequence;

public class StatsCollector {

    private static ArrayBlockingQueue stats = new ArrayBlockingQueue(100);

    public static <T> Function<T> time(final Function<T> function) {
        return new Function<T>() {
            @Override
            public T call() throws Exception {
                Date start = new Date();
                T ret = function.call();
                Date end = new Date();
                if (stats.remainingCapacity() == 0) {
                    stats.take();
                }
                stats.add(Pair.pair(start.getTime(), end.getTime()- start.getTime()));
                return ret;
            }
        };
    }

    public static Sequence<Pair<Long, Number>> stats() {
        List<Pair<Long, Number>> ret = new LinkedList<Pair<Long, Number>>();

        return sequence(ret);
    }
}
