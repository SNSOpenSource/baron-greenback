package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Option;

import java.util.UUID;

public class CheckpointUpdater {
    private final CheckPointHandler checkpointHandler;
    private final UUID id;
    private final Model crawler;

    public CheckpointUpdater(CheckPointHandler checkpointHandler, UUID id, Model crawler) {
        this.checkpointHandler = checkpointHandler;
        this.id = id;
        this.crawler = crawler;
    }

    public void update(Option<?> checkpoint) throws Exception {
        checkpointHandler.updateCheckPoint(id, crawler, checkpoint);
    }
}
