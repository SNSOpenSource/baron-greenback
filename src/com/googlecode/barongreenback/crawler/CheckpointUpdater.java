package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Option;

import java.util.UUID;

public class CheckpointUpdater {
    private final CheckpointHandler checkpointHandler;
    private final UUID id;
    private final Model crawler;

    public CheckpointUpdater(CheckpointHandler checkpointHandler, UUID id, Model crawler) {
        this.checkpointHandler = checkpointHandler;
        this.id = id;
        this.crawler = crawler;
    }

    public void update(Option<?> checkpoint) throws Exception {
        checkpointHandler.updateCheckpoint(id, crawler, checkpoint);
    }
}
