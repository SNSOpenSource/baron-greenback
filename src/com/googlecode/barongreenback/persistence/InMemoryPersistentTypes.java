package com.googlecode.barongreenback.persistence;

import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Unchecked;

import java.util.List;

import static com.googlecode.totallylazy.Callables.second;

public class InMemoryPersistentTypes implements PersistentTypes {

    private final Sequence<Pair<Class<?>, Option<Callable1<StringMappings, StringMapping>>>> typesAndMappings;

    public InMemoryPersistentTypes() {
        this(Sequences.<Pair<Class<?>, Option<Callable1<StringMappings, StringMapping>>>>sequence());
    }

    private InMemoryPersistentTypes(Sequence<Pair<Class<?>, Option<Callable1<StringMappings, StringMapping>>>> typesAndMappings) {
        this.typesAndMappings = typesAndMappings;
    }

    @Override
    public PersistentTypes add(Class<?> klass) {
        return add(klass, Option.<Callable1<StringMappings, StringMapping>>none());
    }

    @Override
    public <T> PersistentTypes add(Class<T> klass, StringMapping<T> mapping) {
        return add(klass, Callables.<StringMappings, StringMapping<T>>ignoreAndReturn(mapping));
    }

    @Override
    public <T> PersistentTypes add(Class<T> klass, Callable1<StringMappings, StringMapping<T>> mappingCallable) {
        return add(klass, Option.some(Unchecked.<Callable1<StringMappings, StringMapping>>cast(mappingCallable)));
    }

    private PersistentTypes add(Class<?> klass, Option<Callable1<StringMappings, StringMapping>> mappingCallable) {
        return new InMemoryPersistentTypes(typesAndMappings.append(Pair.<Class<?>, Option<Callable1<StringMappings, StringMapping>>>pair(klass, mappingCallable)));
    }

    @Override
    public List<Pair<Class<?>, Callable1<StringMappings, StringMapping>>> mappings() {
        return typesAndMappings.map(dropOption()).filter(mappingIsNotNull()).toList();
    }

    @Override
    public List<Class<?>> types() {
        return typesAndMappings.map(Pair.functions.<Class<?>>first()).toList();
    }

    private Predicate<Pair<Class<?>, Callable1<StringMappings, StringMapping>>> mappingIsNotNull() {
        return new Predicate<Pair<Class<?>, Callable1<StringMappings, StringMapping>>>() {
            @Override
            public boolean matches(Pair<Class<?>, Callable1<StringMappings, StringMapping>> other) {
                return other.second() != null;
            }
        };
    }

    private Function1<Pair<Class<?>, Option<Callable1<StringMappings, StringMapping>>>, Pair<Class<?>, Callable1<StringMappings, StringMapping>>> dropOption() {
        return second(Option.functions.<Callable1<StringMappings, StringMapping>>getOrNull());
    }
}
