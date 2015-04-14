package com.googlecode.barongreenback.batch;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Runnables;

import java.io.File;

import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.lastModified;

public class FileRoller extends Function<Void> {
    private final File directory;
    private final int keep;

    public FileRoller(File directory, int keep) {
        this.directory = directory;
        this.keep = keep;
    }

    @Override
    public Void call() throws Exception {
        files(directory).sortBy(descending(lastModified())).drop(keep).each(delete());
        return Runnables.VOID;
    }
}