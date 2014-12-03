package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

public interface CompiledLessCache {
    Option<CompiledLess> get(String key);

    boolean put(String key, CompiledLess result);

    class functions {
        public static Mapper<CompiledLessCache, Option<CompiledLess>> get(final String key) {
            return new Mapper<CompiledLessCache, Option<CompiledLess>>() {
                @Override
                public Option<CompiledLess> call(CompiledLessCache cache) throws Exception {
                    return cache.get(key);
                }
            };
        }

        public static LogicalPredicate<CompiledLessCache> put(final String key, final CompiledLess result) {
            return new LogicalPredicate<CompiledLessCache>() {
                @Override
                public boolean matches(CompiledLessCache cache) {
                    return cache.put(key, result);
                }
            };
        }
    }
}
