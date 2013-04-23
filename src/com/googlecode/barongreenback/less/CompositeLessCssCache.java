package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Functions.or;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CompositeLessCssCache implements LessCssCache {
    private final Sequence<LessCssCache> caches;

    private CompositeLessCssCache(Iterable<LessCssCache> caches) {
        this.caches = sequence(caches);
    }

    public static CompositeLessCssCache compositeLessCssCache(LessCssCache... caches) {
        return new CompositeLessCssCache(sequence(caches));
    }

    public static CompositeLessCssCache compositeLessCssCache(Iterable<LessCssCache> caches) {
        return new CompositeLessCssCache(caches);
    }

    @Override
    public Option<CachedLessCss> get(final String key) {
        return caches.flatMap(functions.get(key)).headOption();
    }

    @Override
    public boolean put(final String key, final CachedLessCss result) {
        return caches.map(functions.put(key, result)).reduce(or);
    }
}