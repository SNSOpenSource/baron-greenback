package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.io.PrintStream;

public interface StagedJob<I> {
    Function<I> getInput(Container container);

    Function1<I, Pair<Sequence<Record>, Sequence<StagedJob<I>>>> process(Container container);

    Function1<Sequence<Record>, Number> write(Records records);
}
