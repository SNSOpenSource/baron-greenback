package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Sequence;
import org.junit.Test;

import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

public class StagedJobComparatorTest {
    @Test
    public void shouldPutMasterJobsFirst() throws Exception {
        Sequence<HttpJob> sorted = sequence(httpJob, masterJob, paginatedJob).sortBy(new MasterJobComparator());
        assertThat(sorted.head(), sameInstance((HttpJob)masterJob));
    }

    @Test
    public void shouldBeAbleToSortByCreationDate() throws Exception {
        Sequence<HttpJob> sorted = sequence(httpJob, masterJob, paginatedJob).sortBy(new MasterJobComparator());
        assertThat(sorted.head(), sameInstance((HttpJob)masterJob));
    }

    private PaginatedHttpJob paginatedJob = PaginatedHttpJob.paginatedHttpJob(null);

    private HttpJob httpJob = HttpJob.httpJob(null);

    private MasterPaginatedHttpJob masterJob = MasterPaginatedHttpJob.masterPaginatedHttpJob(null);
}
