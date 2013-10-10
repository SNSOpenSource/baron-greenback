package com.googlecode.barongreenback.batch;

import com.googlecode.totallylazy.Value;

import java.io.File;

import static com.googlecode.totallylazy.Files.directory;

public class AutoBackupsLocation implements Value<File> {
    private final File value;

    public AutoBackupsLocation(BackupsLocation backupsLocation) {
        this(directory(backupsLocation.value(), "autobackup"));
    }

    public AutoBackupsLocation(File value) {
        this.value = value;
    }

    @Override
    public File value() {
        return value;
    }
}
