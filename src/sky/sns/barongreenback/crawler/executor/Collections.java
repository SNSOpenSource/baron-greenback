package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.util.Collection;
import java.util.Iterator;

public class Collections {
    public static class functions{
        public static <E> Mapper<Collection<E>, Integer> size() {
            return new Mapper<Collection<E>, Integer>() {
                @Override
                public Integer call(Collection<E> queue) throws Exception {
                    return queue.size();
                }
            };
        }

        public static <E> Mapper<Collection<E>, Boolean> empty() {
            return new Mapper<Collection<E>, Boolean>() {
                @Override
                public Boolean call(Collection<E> queue) throws Exception {
                    return queue.isEmpty();
                }
            };
        }

        public static <E> Mapper<Collection<E>, Iterator<E>> iterator() {
            return new Mapper<Collection<E>, Iterator<E>>() {
                @Override
                public Iterator<E> call(Collection<E> queue) throws Exception {
                    return queue.iterator();
                }
            };
        }

        public static <E> Mapper<Collection<E>, Object[]> toArray() {
            return new Mapper<Collection<E>, Object[]>() {
                @Override
                public Object[] call(Collection<E> queue) throws Exception {
                    return queue.toArray();
                }
            };
        }

        public static <E, T> Mapper<Collection<E>, T[]> toArray(final T[] a) {
            return new Mapper<Collection<E>, T[]>() {
                @Override
                public T[] call(Collection<E> queue) throws Exception {
                    return queue.toArray(a);
                }
            };
        }

        public static <E> LogicalPredicate<Collection<E>> containsAll(final Collection<?> c) {
            return new LogicalPredicate<Collection<E>>() {
                @Override
                public boolean matches(Collection<E> queue) {
                    return queue.containsAll(c);
                }
            };
        }
    }
}
