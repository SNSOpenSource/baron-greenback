package com.googlecode.barongreenback.jobshistory;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.jobs.InMemoryJobsStorage;
import com.googlecode.utterlyidle.jobs.JobsStorage;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.services.Services;
import com.googlecode.utterlyidle.services.ServicesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class JobsHistoryModule implements ResourcesModule, RequestScopedModule, ApplicationScopedModule, ServicesModule {
    @Override
    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(JobsHistoryResource.class));
    }

    @Override
    public Container addPerRequestObjects(Container container) throws Exception {
        return container.add(JobsHistoryRepository.class, RecordsJobsHistoryRepository.class).
                add(JobsStorage.class, HistoryRecordingJobsStorage.class).
                add(JobHistoryItemLifespanInHours.class).
                add(JobsHistoryCleanupService.class);
    }

    @Override
    public Container addPerApplicationObjects(Container container) throws Exception {
        container.remove(JobsStorage.class);
        return container.add(InMemoryJobsStorage.class);
    }

    @Override
    public Services add(Services services) throws Exception {
        return services.add(JobsHistoryCleanupService.class);
    }
}


