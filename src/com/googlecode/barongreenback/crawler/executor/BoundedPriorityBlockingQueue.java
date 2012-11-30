package com.googlecode.barongreenback.crawler.executor;

import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedPriorityBlockingQueue<E> implements BlockingQueue<E> {
    
    private final int capacity;
    
    private final PriorityQueue<E> decorated;
    
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition full = lock.newCondition();
    private final Condition empty = lock.newCondition();

    public BoundedPriorityBlockingQueue(int capacity) {
        super();
        this.capacity = capacity;
        this.decorated = new PriorityQueue<E>(capacity);
    }
    
    public BoundedPriorityBlockingQueue() {
        super();
        this.decorated = new PriorityQueue<E>();
        this.capacity = Integer.MAX_VALUE;
    }

    @Override
    public boolean offer(E e) {
        try {
            lock.lock();
            if (!isFull()) {
                return decorated.offer(e);
            } else {
                return false;
            }
        } finally {
            empty.signalAll();
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
            return decorated.remove();
        } finally {
            full.signalAll();
            lock.unlock();
        }
    }

    @Override
    public E poll() {
        try {
            lock.lock();
            return decorated.poll();
        } finally {
            full.signalAll();
            lock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        try {
            lock.lock();
            return decorated.addAll(c);
        } finally {
            empty.signalAll();
            lock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        try {
            lock.lock();
            return decorated.removeAll(c);
        } finally {
            full.signalAll();
            lock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        try {
            lock.lock();
            return decorated.retainAll(c);
        } finally {
            full.signalAll();
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        try {
            lock.lock();
            decorated.clear();
        } finally {
            full.signalAll();
            lock.unlock();
        }
    }

    @Override
    public boolean add(E e) {
        try {
            lock.lock();
            return decorated.add(e);
        } finally {
            empty.signalAll();
            lock.unlock();
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        try {
            lock.lock();
            while (!offer(e)) {
                full.await();
            }
        } finally {
            empty.signalAll();
            lock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        try {
            lock.lock();
            return decorated.remove(o);
        } finally {
            full.signalAll();
            lock.unlock();
        }
    }
    
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException {
        try {
            lock.lock();
            if (!isFull()) {
                boolean timedout = full.await(timeout, unit);
                if (timedout) {
                    return false;
                }
            }
            return decorated.offer(e);
        } finally {
            empty.signalAll();
            lock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        try {
            lock.lock();
            while (isEmpty()) {
                empty.await();
            }
            return decorated.poll();
        } finally {
            full.signalAll();
            lock.unlock();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            lock.lock();
            while (isEmpty()) {
                boolean timedout = empty.await(timeout, unit);
                if (timedout) {
                    return null;
                }
            }
            return decorated.poll();
        } finally {
            full.signalAll();
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
            full.signalAll();
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
            full.signalAll();
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
            return decorated.contains(o);
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
            return decorated.element();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E peek() {
        try {
            lock.lock();
            return decorated.peek();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.lock();
            return decorated.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            lock.lock();
            return decorated.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        try {
            lock.lock();
            return decorated.iterator();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        try {
            lock.lock();
            return decorated.toArray();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        try {
            lock.lock();
            return decorated.toArray(a);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        try {
            lock.lock();
            return decorated.containsAll(c);
        } finally {
            lock.unlock();
        }
    }
}
