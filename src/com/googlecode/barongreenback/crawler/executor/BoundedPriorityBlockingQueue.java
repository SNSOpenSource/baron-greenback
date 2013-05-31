package com.googlecode.barongreenback.crawler.executor;

import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedPriorityBlockingQueue<E> implements BlockingQueue<E> {
    private final int capacity;
    private final Queue<E> queue;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition space = lock.newCondition();
    private final Condition item = lock.newCondition();

    public BoundedPriorityBlockingQueue(int capacity, Queue<E> queue) {
        this.capacity = capacity;
        this.queue = queue;
    }

    public BoundedPriorityBlockingQueue(int capacity) {
        this(capacity, new PriorityQueue<E>());
    }

    public BoundedPriorityBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    @Override
    public boolean offer(E e) {
        try {
            lock.lock();
            return !isFull() && queue.offer(e);
        } finally {
            item.signal();
            lock.unlock();
        }
    }
    
    private boolean isFull() {
        return size() >= capacity;
    }

    @Override
    public E remove() {
        try {
            lock.lock();
            return queue.remove();
        } finally {
            space.signal();
            lock.unlock();
        }
    }

    @Override
    public E poll() {
        try {
            lock.lock();
            return queue.poll();
        } finally {
            space.signal();
            lock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        try {
            lock.lock();
            return queue.addAll(c);
        } finally {
            item.signal();
            lock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        try {
            lock.lock();
            return queue.removeAll(c);
        } finally {
            space.signal();
            lock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        try {
            lock.lock();
            return queue.retainAll(c);
        } finally {
            space.signal();
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        try {
            lock.lock();
            queue.clear();
        } finally {
            space.signal();
            lock.unlock();
        }
    }

    @Override
    public boolean add(E e) {
        try {
            lock.lock();
            return queue.add(e);
        } finally {
            item.signal();
            lock.unlock();
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        try {
            lock.lock();
            while (!offer(e)) {
                space.await();
            }
        } finally {
            item.signal();
            lock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        try {
            lock.lock();
            return queue.remove(o);
        } finally {
            space.signal();
            lock.unlock();
        }
    }
    
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException {
        try {
            lock.lock();
            if (!isFull()) {
                if (space.await(timeout, unit)) {
                    return false;
                }
            }
            return queue.offer(e);
        } finally {
            item.signal();
            lock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        try {
            lock.lock();
            while (isEmpty()) {
                item.await();
            }
            return queue.poll();
        } finally {
            space.signal();
            lock.unlock();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            lock.lock();
            while (isEmpty()) {
                if (item.await(timeout, unit)) {
                    return null;
                }
            }
            return queue.poll();
        } finally {
            space.signal();
            lock.unlock();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        int changes = 0;
        try {
            lock.lock();
            for (E e : this) {
                remove(e);
                changes++;
                c.add(e);
            }
        } finally {
            space.signal();
            lock.unlock();
        }
        return changes;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        int changes = 0;
        try {
            lock.lock();
            Iterator<E> iterator = this.iterator();
            while (iterator.hasNext() && changes <= maxElements ) {
                E e = iterator.next();
                remove(e);
                changes++;
                c.add(e);
            }
        } finally {
            space.signal();
            lock.unlock();
        }
        return changes;
    }

    
    //
    // Non-mutating methods
    //
    
    @Override
    public boolean contains(Object o) {
        try {
            lock.lock();
            return queue.contains(o);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        try {
            lock.lock();
            return capacity - size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E element() {
        try {
            lock.lock();
            return queue.element();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E peek() {
        try {
            lock.lock();
            return queue.peek();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.lock();
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            lock.lock();
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        try {
            lock.lock();
            return queue.iterator();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        try {
            lock.lock();
            return queue.toArray();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        try {
            lock.lock();
            return queue.toArray(a);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        try {
            lock.lock();
            return queue.containsAll(c);
        } finally {
            lock.unlock();
        }
    }
}
