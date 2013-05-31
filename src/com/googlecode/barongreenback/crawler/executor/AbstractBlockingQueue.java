package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.callables.Count;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.googlecode.barongreenback.crawler.executor.Queues.functions;
import static com.googlecode.totallylazy.Callers.call;
import static com.googlecode.totallylazy.LazyException.lazyException;
import static com.googlecode.totallylazy.Sequences.sequence;

public abstract class AbstractBlockingQueue<E> implements BlockingQueue<E> {
    protected final Queue<E> queue;
    protected final ReentrantLock lock = new ReentrantLock(true);
    protected final Condition space = lock.newCondition();
    protected final Condition item = lock.newCondition();

    protected AbstractBlockingQueue(Queue<E> queue) {
        this.queue = queue;
    }

    protected <T> T addItem(Callable1<? super Queue<E>, ? extends T> callable) {
        return removeItem(RuntimeException.class, callable);
    }

    protected <T, Ex extends Exception> T addItem(Class<Ex> exception, Callable1<? super Queue<E>, ? extends T> callable) throws Ex {
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

    protected <T> T removeItem(Callable1<? super Queue<E>, ? extends T> callable) {
        return removeItem(RuntimeException.class, callable);
    }

    protected <T, Ex extends Exception> T removeItem(Class<Ex> exception, Callable1<? super Queue<E>, ? extends T> callable) throws Ex {
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

    protected <T> T lock(Callable1<? super Queue<E>, ? extends T> callable) {
        try {
            lock.lock();
            return call(callable, queue);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(E e) {
        return addItem(functions.offer(e));
    }

    @Override
    public E remove() {
        return removeItem(functions.<E>remove());
    }

    @Override
    public E poll() {
        return removeItem(functions.<E>poll());
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return addItem(functions.addAll(c));
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return removeItem(functions.<E>removeAll(c));
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return removeItem(functions.<E>retainAll(c));
    }

    @Override
    public void clear() {
        removeItem(functions.<E>clear());
    }

    @Override
    public boolean add(final E e) {
        return addItem(functions.add(e));
    }

    @Override
    public boolean remove(final Object o) {
        return removeItem(functions.<E>remove(o));
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
                return sequence(queue).
                        take(maxElements).
                        realise().
                        map(functions.<E>remove(queue)).
                        reduce(Count.count()).intValue();
            }
        });
    }

    @Override
    public boolean contains(final Object o) {
        return lock(Predicates.<E>contains(Unchecked.<E>cast(o)));
    }

    @Override
    public E element() {
        return lock(functions.<E>element());
    }

    @Override
    public E peek() {
        return lock(functions.<E>peek());
    }

    @Override
    public int size() {
        return lock(functions.<E>size());
    }

    @Override
    public boolean isEmpty() {
        return lock(functions.<E>empty());
    }

    @Override
    public Iterator<E> iterator() {
        return lock(functions.<E>iterator());
    }

    @Override
    public Object[] toArray() {
        return lock(functions.<E>toArray());
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return lock(functions.<E, T>toArray(a));
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return lock(functions.<E>containsAll(c));
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
}
