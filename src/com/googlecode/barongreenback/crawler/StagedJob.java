package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Application;
import com.googlecode.yadic.Container;

public interface StagedJob<I> {
    Function<I> getInput(Container container);

    Function1<I, Pair<Sequence<Record>, Sequence<StagedJob<I>>>> process(Container container);

    Function1<Sequence<Record>, Number> write(Application application);
}
