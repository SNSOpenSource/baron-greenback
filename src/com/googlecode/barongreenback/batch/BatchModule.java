package com.googlecode.barongreenback.batch;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class BatchModule implements ResourcesModule, RequestScopedModule {
    @Override
    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(BatchResource.class));
    }

    @Override
    public Container addPerRequestObjects(Container container) throws Exception {
        return container.
                add(KeepBackups.class).
                add(BackupStart.class).
                add(BackupInterval.class).
                addActivator(FileRoller.class, BackupFileRollerActivator.class);
    }
}