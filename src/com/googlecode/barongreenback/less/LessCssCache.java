package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

public interface LessCssCache {
    Option<CachedLessCss> get(String key);

    boolean put(String key, CachedLessCss result);

    class functions {
        public static Mapper<LessCssCache, Option<CachedLessCss>> get(final String key) {
            return new Mapper<LessCssCache, Option<CachedLessCss>>() {
                @Override
                public Option<CachedLessCss> call(LessCssCache cache) throws Exception {
                    return cache.get(key);
                }
            };
        }

        public static LogicalPredicate<LessCssCache> put(final String key, final CachedLessCss result) {
            return new LogicalPredicate<LessCssCache>() {
                @Override
                public boolean matches(LessCssCache cache) {
                    return cache.put(key, result);
                }
            };
        }
    }
}
