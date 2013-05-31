package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.callables.Count;

import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.googlecode.barongreenback.crawler.executor.Queues.functions;
import static com.googlecode.totallylazy.Callers.call;
import static com.googlecode.totallylazy.LazyException.lazyException;
import static com.googlecode.totallylazy.Sequences.sequence;

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
    public boolean offer(final E e) {
        return addItem(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return !isFull() && queue.offer(e);
            }
        });
    }

    @Override
    public E remove() {
        return removeItem(functions.<E>head());
    }

    @Override
    public E poll() {
        return removeItem(functions.<E>poll());
    }

    // TODO Fix me to take into account capacity
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return addItem(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return queue.addAll(c);
            }
        });
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return removeItem(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return queue.removeAll(c);
            }
        });
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return removeItem(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return queue.retainAll(c);
            }
        });
    }

    @Override
    public void clear() {
        removeItem(new Block<Queue<E>>() {
            @Override
            protected void execute(Queue<E> queue) throws Exception {
                queue.clear();
            }
        });
    }

    // TODO Fix me to take into account capacity
    @Override
    public boolean add(final E e) {
        return addItem(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return queue.add(e);
            }
        });
    }

    @Override
    public void put(final E e) throws InterruptedException {
        addItem(InterruptedException.class, new Block<Queue<E>>() {
            @Override
            protected void execute(Queue<E> queue) throws Exception {
                while (!offer(e)) {
                    space.await();
                }
            }
        });
    }

    @Override
    public boolean remove(final Object o) {
        return removeItem(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return queue.remove(o);
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
    public E take() throws InterruptedException {
        return removeItem(InterruptedException.class, new Mapper<Queue<E>, E>() {
            @Override
            public E call(Queue<E> queue) throws Exception {
                while (isEmpty()) {
                    item.await();
                }
                return queue.poll();
            }
        });
    }

    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return removeItem(InterruptedException.class, new Mapper<Queue<E>, E>() {
            @Override
            public E call(Queue<E> queue) throws Exception {
                while (isEmpty()) {
                    if (item.await(timeout, unit)) {
                        return null;
                    }
                }
                return queue.poll();
            }
        });
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        return drainTo(c, c.size());
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        return removeItem(new Mapper<Queue<E>, Integer>() {
            @Override
            public Integer call(Queue<E> queue) throws Exception {
                return sequence(BoundedPriorityBlockingQueue.this).
                        take(maxElements).
                        map(functions.<E>remove().apply(queue)).
                        reduce(Count.count()).intValue();
            }
        });
    }


    //
    // Non-mutating methods
    //

    @Override
    public boolean contains(final Object o) {
        return lock(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return queue.contains(o);
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

    @Override
    public E element() {
        return lock(new Mapper<Queue<E>, E>() {
            @Override
            public E call(Queue<E> queue) throws Exception {
                return queue.element();
            }
        });
    }

    @Override
    public E peek() {
        return lock(new Mapper<Queue<E>, E>() {
            @Override
            public E call(Queue<E> queue) throws Exception {
                return queue.peek();
            }
        });
    }

    @Override
    public int size() {
        return lock(new Mapper<Queue<E>, Integer>() {
            @Override
            public Integer call(Queue<E> queue) throws Exception {
                return queue.size();
            }
        });
    }

    @Override
    public boolean isEmpty() {
        return lock(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return queue.isEmpty();
            }
        });
    }

    //TODO fix me to be a snapshot
    @Override
    public Iterator<E> iterator() {
        return lock(new Mapper<Queue<E>, Iterator<E>>() {
            @Override
            public Iterator<E> call(Queue<E> queue) throws Exception {
                return queue.iterator();
            }
        });
    }

    @Override
    public Object[] toArray() {
        return lock(new Mapper<Queue<E>, Object[]>() {
            @Override
            public Object[] call(Queue<E> queue) throws Exception {
                return queue.toArray();
            }
        });
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return lock(new Mapper<Queue<E>, T[]>() {
            @Override
            public T[] call(Queue<E> queue) throws Exception {
                return queue.toArray(a);
            }
        });
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return lock(new Mapper<Queue<E>, Boolean>() {
            @Override
            public Boolean call(Queue<E> queue) throws Exception {
                return queue.containsAll(c);
            }
        });
    }

    private <T> T addItem(Callable1<? super Queue<E>, ? extends T> callable) {
        return removeItem(RuntimeException.class, callable);
    }

    private <T, Ex extends Exception> T addItem(Class<Ex> exception, Callable1<? super Queue<E>, ? extends T> callable) throws Ex {
        try {
            lock.lock();
            try {
                return callable.call(queue);
            } catch (Exception e) {
                throw lazyException(e).unwrap(exception);
            }
        } finally {
            item.signal();
            lock.unlock();
        }
    }

    private <T> T removeItem(Callable1<? super Queue<E>, ? extends T> callable) {
        return removeItem(RuntimeException.class, callable);
    }

    private <T, Ex extends Exception> T removeItem(Class<Ex> exception, Callable1<? super Queue<E>, ? extends T> callable) throws Ex {
        try {
            lock.lock();
            try {
                return callable.call(queue);
            } catch (Exception e) {
                throw lazyException(e).unwrap(exception);
            }
        } finally {
            space.signal();
            lock.unlock();
        }
    }

    private <T> T lock(Callable1<? super Queue<E>, ? extends T> callable) {
        try {
            lock.lock();
            return call(callable, queue);
        } finally {
            lock.unlock();
        }
    }

    private boolean isFull() {
        return size() >= capacity;
    }


}
