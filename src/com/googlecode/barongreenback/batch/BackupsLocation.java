package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Value;

import java.io.File;

public class BackupsLocation implements Value<File> {
    private static final String PROPERTY_NAME = "backups.location";
    private final File value;

    public BackupsLocation(BaronGreenbackProperties properties) {
        this(new File(properties.getProperty(PROPERTY_NAME, Files.temporaryDirectory().getAbsolutePath())));
    }

    public BackupsLocation(File value) {
        this.value = value;
    }

    @Override
    public File value() {
        return value;
    }
}
