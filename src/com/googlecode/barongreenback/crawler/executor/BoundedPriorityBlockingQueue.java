package com.googlecode.barongreenback.crawler.executor;

import java.util.PriorityQueue;

public class BoundedPriorityBlockingQueue<E> extends BoundedBlockingQueue<E> {
    public BoundedPriorityBlockingQueue(int capacity) {
        super(capacity, new PriorityQueue<E>());
    }

    public BoundedPriorityBlockingQueue() {
        this(Integer.MAX_VALUE);
    }
}
