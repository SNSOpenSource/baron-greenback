package com.googlecode.barongreenback.batch;

import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.batch.BatchResource.BACKUP_LOCATION;

public class BackupFileRollerActivator implements Callable<FileRoller> {
    private KeepBackups keepBackups;

    public BackupFileRollerActivator(KeepBackups keepBackups) {
        this.keepBackups = keepBackups;
    }

    @Override
    public FileRoller call() throws Exception {
        return new FileRoller(BACKUP_LOCATION, keepBackups.value());
    }
}
