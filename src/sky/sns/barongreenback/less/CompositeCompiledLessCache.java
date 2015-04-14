package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Functions.or;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CompositeCompiledLessCache implements CompiledLessCache {
    private final Sequence<CompiledLessCache> caches;

    private CompositeCompiledLessCache(Iterable<CompiledLessCache> caches) {
        this.caches = sequence(caches);
    }

    public static CompositeCompiledLessCache compositeLessCssCache(CompiledLessCache... caches) {
        return new CompositeCompiledLessCache(sequence(caches));
    }

    public static CompositeCompiledLessCache compositeLessCssCache(Iterable<CompiledLessCache> caches) {
        return new CompositeCompiledLessCache(caches);
    }

    @Override
    public Option<CompiledLess> get(final String key) {
        return caches.flatMap(functions.get(key)).headOption();
    }

    @Override
    public boolean put(final String key, final CompiledLess result) {
        return caches.map(functions.put(key, result)).reduce(or);
    }
}