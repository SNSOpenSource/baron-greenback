package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.totallylazy.Mapper;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class BoundedBlockingQueue<E> extends AbstractBlockingQueue<E> {
    private final int capacity;

    public BoundedBlockingQueue(int capacity, Queue<E> queue) {
        super(queue);
        this.capacity = capacity;
    }

    public BoundedBlockingQueue(int capacity) {
        this(capacity, new PriorityQueue<E>());
    }

    public BoundedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    @Override
    public boolean offer(final E e) {
        return addItem(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return !isFull() && queue.offer(e);
            }
        });
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return addItem(InterruptedException.class, new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                if (!isFull()) {
                    if (space.await(timeout, unit)) {
                        return false;
                    }
                }
                return queue.offer(e);
            }
        });
    }

    @Override
    public int remainingCapacity() {
        return lock(new Mapper<Queue<E>, Integer>() {
            @Override
            public Integer call(Queue<E> queue) throws Exception {
                return capacity - size();
            }
        });
    }

    private boolean isFull() {
        return size() >= capacity;
    }


}
