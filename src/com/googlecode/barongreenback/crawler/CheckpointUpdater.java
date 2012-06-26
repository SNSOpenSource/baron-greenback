package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;

public class CheckpointUpdater {
    private final Function1<Option<?>, Void> updater;

    public CheckpointUpdater(Function1<Option<?>, Void> updater) {
        this.updater = updater;
    }

    public void update(Option<?> value) throws Exception {
        updater.call(value);
    }
}
