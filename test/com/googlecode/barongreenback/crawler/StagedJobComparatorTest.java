package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Sequence;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.googlecode.barongreenback.crawler.HttpJob.httpJob;
import static com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob.masterPaginatedHttpJob;
import static com.googlecode.barongreenback.crawler.PaginatedHttpJob.paginatedHttpJob;
import static com.googlecode.barongreenback.crawler.StagedJobComparator.masterJobsFirst;
import static com.googlecode.barongreenback.crawler.StagedJobComparator.masterJobsThenNewest;
import static com.googlecode.barongreenback.crawler.StagedJobComparator.newestJobsFirst;
import static com.googlecode.funclate.Model.persistent.model;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

public class StagedJobComparatorTest {
    @Test
    public void shouldPutMasterJobsFirst() throws Exception {
        Sequence<HttpJob> sorted = sequence(httpJob, masterJob, paginatedJob).sortBy(masterJobsFirst());
        assertThat(sorted.head(), sameInstance((HttpJob) masterJob));
    }

    @Test
    public void shouldBeAbleToSortByCreationDate() throws Exception {
        HttpJob olderJob = httpJobFor(date(2000, 1, 1));
        HttpJob newerJob = httpJobFor(date(2001, 1, 1));
        Sequence<HttpJob> sorted = sequence(olderJob, newerJob).sortBy(newestJobsFirst());
        assertThat(sorted.first(), sameInstance(newerJob));
        assertThat(sorted.second(), sameInstance(olderJob));
    }

    @Test
    public void canSortByMasterJobsThenCreatedDate() throws Exception {
        HttpJob oldestJob = httpJobFor(date(1999, 1, 1));
        HttpJob olderMasterJob = masterJobFor(date(2000, 1, 1));
        HttpJob newerMasterJob = masterJobFor(date(2001, 1, 1));
        HttpJob newestJob = httpJobFor(date(2002, 1, 1));

        List<HttpJob> sorted = sequence(oldestJob, olderMasterJob, newerMasterJob, newestJob).sortBy(masterJobsThenNewest()).toList();
        assertThat(sorted.get(0), sameInstance(newerMasterJob));
        assertThat(sorted.get(1), sameInstance(olderMasterJob));
        assertThat(sorted.get(2), sameInstance(newestJob));
        assertThat(sorted.get(3), sameInstance(oldestJob));

    }

    private HttpJob httpJobFor(Date createdDate) {
        return httpJob(created(createdDate));
    }

    private HttpJob masterJobFor(Date createdDate) {
        return masterPaginatedHttpJob(created(createdDate));
    }

    private Model created(Date createdDate) {
        return model().set("createdDate", createdDate);
    }

    private PaginatedHttpJob paginatedJob = paginatedHttpJob(null);
    private HttpJob httpJob = HttpJob.httpJob(null);
    private MasterPaginatedHttpJob masterJob = masterPaginatedHttpJob(null);
}
