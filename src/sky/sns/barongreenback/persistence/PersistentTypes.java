package com.googlecode.barongreenback.persistence;

import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;

import java.util.List;

public interface PersistentTypes {
    PersistentTypes add(Class<?> klass);

    <T> PersistentTypes add(Class<T> klass, StringMapping<T> mapping);

    <T> PersistentTypes add(Class<T> klass, Callable1<StringMappings, StringMapping<T>> mappingCallable);

    List<Pair<Class<?>, Callable1<StringMappings, StringMapping>>> mappings();

    List<Class<?>> types();
}
