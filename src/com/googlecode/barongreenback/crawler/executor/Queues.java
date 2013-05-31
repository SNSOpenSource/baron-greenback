package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Mapper;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class Queues {
    public static class functions extends Collections.functions {
        public static <E> Mapper<Queue<E>, Boolean> offer(final E e) {
            return new Mapper<Queue<E>, Boolean>() {
                @Override
                public Boolean call(Queue<E> queue) throws Exception {
                    return queue.offer(e);
                }
            };
        }

        public static <E> Mapper<Queue<E>, E> remove() {
            return new Mapper<Queue<E>, E>() {
                @Override
                public E call(Queue<E> queue) throws Exception {
                    return queue.remove();
                }
            };
        }

        public static <E> Mapper<E, Boolean> remove(final Queue<E> queue) {
            return new Mapper<E, Boolean>() {
                @Override
                public Boolean call(E e) throws Exception {
                    return queue.remove(e);
                }
            };
        }

        public static <E> Mapper<Queue<E>, Boolean> remove(final Object o) {
            return new Mapper<Queue<E>, Boolean>() {
                @Override
                public Boolean call(Queue<E> queue) throws Exception {
                    return queue.remove(o);
                }
            };
        }

        public static <E> Mapper<Queue<E>, E> poll() {
            return new Mapper<Queue<E>, E>() {
                @Override
                public E call(Queue<E> queue) throws Exception {
                    return queue.poll();
                }
            };
        }

        public static <E> Mapper<Queue<E>, Boolean> addAll(final Collection<? extends E> c) {
            return new Mapper<Queue<E>, Boolean>() {
                @Override
                public Boolean call(Queue<E> queue) throws Exception {
                    return queue.addAll(c);
                }
            };
        }

        public static <E> Mapper<Queue<E>, Boolean> removeAll(final Collection<?> c) {
            return new Mapper<Queue<E>, Boolean>() {
                @Override
                public Boolean call(Queue<E> queue) throws Exception {
                    return queue.removeAll(c);
                }
            };
        }

        public static <E> Mapper<Queue<E>, Boolean> retainAll(final Collection<?> c) {
            return new Mapper<Queue<E>, Boolean>() {
                @Override
                public Boolean call(Queue<E> queue) throws Exception {
                    return queue.retainAll(c);
                }
            };
        }

        public static <E> Block<Queue<E>> clear() {
            return new Block<Queue<E>>() {
                @Override
                protected void execute(Queue<E> queue) throws Exception {
                    queue.clear();
                }
            };
        }

        public static <E> Mapper<Queue<E>, Boolean> add(final E e) {
            return new Mapper<Queue<E>, Boolean>() {
                @Override
                public Boolean call(Queue<E> queue) throws Exception {
                    return queue.add(e);
                }
            };
        }

        public static <E> Block<BlockingQueue<E>> put(final E e) {
            return new Block<BlockingQueue<E>>() {
                @Override
                protected void execute(BlockingQueue<E> queue) throws Exception {
                    queue.put(e);
                }
            };
        }

        public static <E> Mapper<Queue<E>, E> element() {
            return new Mapper<Queue<E>, E>() {
                @Override
                public E call(Queue<E> queue) throws Exception {
                    return queue.element();
                }
            };
        }

        public static <E> Mapper<Queue<E>, E> peek() {
            return new Mapper<Queue<E>, E>() {
                @Override
                public E call(Queue<E> queue) throws Exception {
                    return queue.peek();
                }
            };
        }
    }
}
