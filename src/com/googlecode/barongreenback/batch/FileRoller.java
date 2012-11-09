package com.googlecode.barongreenback.batch;

import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;

import java.io.File;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.lastModified;

public class FileRoller implements Callable<Void> {
    private final File directory;
    private final int keep;

    public FileRoller(File directory, int keep) {
        this.directory = directory;
        this.keep = keep;
    }

    @Override
    public Void call() throws Exception {
        Sequence<File> files = files(directory);
        Sequence<File> files1 = files.sortBy(descending(lastModified()));
        Sequence<File> drop = files1.drop(keep);
        drop.each(delete());
        return Runnables.VOID;
    }
}