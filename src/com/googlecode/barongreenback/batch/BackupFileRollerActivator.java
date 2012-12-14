package com.googlecode.barongreenback.batch;

import java.util.concurrent.Callable;

public class BackupFileRollerActivator implements Callable<FileRoller> {
    private KeepBackups keepBackups;
    private AutoBackupsLocation autoBackupsLocation;

    public BackupFileRollerActivator(KeepBackups keepBackups, AutoBackupsLocation autoBackupsLocation) {
        this.keepBackups = keepBackups;
        this.autoBackupsLocation = autoBackupsLocation;
    }

    @Override
    public FileRoller call() throws Exception {
        return new FileRoller(autoBackupsLocation.value(), keepBackups.value());
    }
}
