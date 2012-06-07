package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.io.PrintStream;

public interface StagedJob<I> {
    Function<I> getInput(Container container);

    Function1<I, Pair<Sequence<Record>, Sequence<StagedJob<I>>>> process();

    Function1<Sequence<Record>, Number> write(Records records);
}
