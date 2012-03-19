package com.googlecode.barongreenback.queues;

import com.googlecode.totallylazy.Callable1;

import java.util.concurrent.Callable;

public interface Completer {
    <T> void complete(Callable<T> task, Callable1<T, ?> completion);
}
