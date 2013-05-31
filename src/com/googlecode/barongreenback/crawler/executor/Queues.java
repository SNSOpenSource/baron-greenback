package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Mapper;

import java.util.Queue;

public class Queues {
    public static class functions {
        public static <E> Mapper<Queue<E>, Boolean> offer(final E e) {
            return new Mapper<Queue<E>, Boolean>() {
                @Override
                public Boolean call(Queue<E> queue) throws Exception {
                    return queue.offer(e);
                }
            };
        }

        public static <E> Mapper<Queue<E>, E> head() {
            return new Mapper<Queue<E>, E>() {
                @Override
                public E call(Queue<E> queue) throws Exception {
                    return queue.remove();
                }
            };
        }

        public static <E>Function2<Queue<E>, E, Boolean> remove() {
            return new Function2<Queue<E>, E, Boolean>() {
                @Override
                public Boolean call(Queue<E> queue, E e) throws Exception {
                    return queue.remove(e);
                }
            };
        }

        public static <E> Mapper<Queue<E>, Boolean> remove(final E o) {
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
    }
}
