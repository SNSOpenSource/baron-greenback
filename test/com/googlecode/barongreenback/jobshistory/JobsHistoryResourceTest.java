package com.googlecode.barongreenback.jobshistory;

import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.barongreenback.shared.DateRenderer;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.totallylazy.time.Days;
import com.googlecode.totallylazy.time.Hours;
import com.googlecode.totallylazy.time.Minutes;
import com.googlecode.totallylazy.time.Seconds;
import com.googlecode.totallylazy.time.StoppedClock;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.jobs.CompletedJob;
import com.googlecode.utterlyidle.jobs.CreatedJob;
import com.googlecode.utterlyidle.jobs.Job;
import com.googlecode.utterlyidle.jobs.JobsStorage;
import com.googlecode.utterlyidle.jobs.RunningJob;
import com.googlecode.yadic.Container;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.googlecode.barongreenback.jobshistory.JobsHistoryPage.JobHistoryItem.functions.timestamp;
import static com.googlecode.barongreenback.jobshistory.JobsHistoryResource.queryForOlderThan;
import static com.googlecode.barongreenback.shared.DateRenderer.DEFAULT_DATE_FORMAT;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.trim;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.totallylazy.time.Dates.date;
import static com.googlecode.totallylazy.time.Dates.parse;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.lang.String.format;
import static java.util.Calendar.DAY_OF_MONTH;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JobsHistoryResourceTest extends ApplicationTests {

    public static final String CRAWLER_UUID = "77916239-0dfe-4217-9e2a-ceaa9e5bed42";

    @Test
    public void displaysNoJobHistory() throws Exception {
        final JobsHistoryPage historyPage = new JobsHistoryPage(browser);
        assertThat(historyPage.items().size(), is(0));
    }

    @Test
    public void displaysHistoryItemForCreatedJob() throws Exception {
        final Request request = post("/foo").header("X-HAM-NAME", "PROSCIUTTO").form("id", CRAWLER_UUID).build();
        final CreatedJob createdJob = jobCreatedAt(date(1983, 10, 7), request);
        jobStorageContains(createdJob);

        final JobsHistoryPage historyPage = new JobsHistoryPage(browser);
        assertThat(historyPage.items().size(), is(1));
        assertThat(historyPage.items().get(0).getTimestamp(), containsString("1983/10/07"));
        assertThat(historyPage.items().get(0).getJobId(), containsLinkFor(createdJob.id().toString()));
        assertThat(historyPage.items().get(0).getAction(), containsString("created"));
        assertThat(historyPage.items().get(0).getShortMessage(), containsString("/foo"));
        assertThat(historyPage.items().get(0).getRawMessage(), is(request.toString()));
        assertThat(historyPage.items().get(0).getElapsedTime().trim(), is("0"));
    }

    @Test
    public void displaysHistoryItemForStartedJob() throws Exception {
        final RunningJob startedJob = jobCreatedAt(date(1900, 1, 1, 12, 0, 0)).start(new StoppedClock(date(1900, 1, 1, 12, 0, 5)));
        jobStorageContains(startedJob);

        final JobsHistoryPage historyPage = new JobsHistoryPage(browser);
        assertThat(historyPage.items().size(), is(1));
        assertThat(historyPage.items().get(0).getTimestamp(), containsString("1900/01/01"));
        assertThat(historyPage.items().get(0).getJobId(), containsLinkFor(startedJob.id().toString()));
        assertThat(historyPage.items().get(0).getAction(), containsString("started"));
        assertThat(historyPage.items().get(0).getShortMessage().trim(), is(""));
        assertThat(historyPage.items().get(0).getElapsedTime().trim(), is("5"));
    }

    @Test
    public void displaysHistoryItemForCompletedJob() throws Exception {
        final Date creationTime = date(1900, 1, 1, 12, 0, 0);
        final Response response = response().build();
        final CompletedJob completedJob = jobCreatedAt(creationTime).start(new StoppedClock(creationTime)).complete(response, new StoppedClock(Seconds.add(creationTime, 15)));
        jobStorageContains(completedJob);

        final JobsHistoryPage historyPage = new JobsHistoryPage(browser);
        assertThat(historyPage.items().size(), is(1));
        assertThat(historyPage.items().get(0).getTimestamp(), containsString("1900/01/01"));
        assertThat(historyPage.items().get(0).getJobId(), containsLinkFor(completedJob.id().toString()));
        assertThat(historyPage.items().get(0).getAction(), containsString("completed"));
        assertThat(historyPage.items().get(0).getShortMessage(), containsString("200 OK"));
        assertThat(historyPage.items().get(0).getRawMessage(), is(response.toString()));
        assertThat(historyPage.items().get(0).getElapsedTime().trim(), is("15"));
    }

    @Test
    public void displaysHumanReadableShortMessageForCreatedJobRequest() throws Exception {
        final CreatedJob createdJob = jobCreatedAt(date(1983, 10, 7));
        jobStorageContains(createdJob);

        final JobsHistoryPage historyPage = new JobsHistoryPage(browser);
        assertThat(historyPage.items().size(), is(1));
        assertThat(historyPage.items().get(0).getShortMessage(), is("POST /foo id=" + CRAWLER_UUID));
    }

    @Test
    public void displaysHumanReadableShortMessageForCompletedJobResponse() throws Exception {
        final Date completionDate = date(1985, 7, 6);
        final Response response = response(Status.OK).entity("Updated 22 Records").build();
        final CompletedJob completedJob = startedJobStartedAt(Minutes.subtract(completionDate, 1)).complete(response, new StoppedClock(completionDate));
        jobStorageContains(completedJob);

        final JobsHistoryPage historyPage = new JobsHistoryPage(browser);
        assertThat(historyPage.items().size(), is(1));
        assertThat(historyPage.items().get(0).getShortMessage(), is("200 OK Updated 22 Records"));
    }

    @Test
    public void resultsArePaginated() throws Exception {
        jobStorageContains(jobs(10));

        final JobsHistoryPage firstPage = new JobsHistoryPage(browser, 8);
        assertThat(firstPage.items().size(), is(8));

        final JobsHistoryPage secondPage = firstPage.page(2);
        assertThat(secondPage.items().size(), is(2));
    }

    @Test
    public void resultsCanBeSorted() throws Exception {
        jobStorageContains(Numbers.range(1, 9).map(new Function1<Number, Job>() {
            @Override
            public Job call(Number number) throws Exception {
                return CreatedJob.createJob(get("/" + number.toString()).build(), new StoppedClock(new Date(946598400000L + (number.intValue() * 86400000))));
            }
        }).shuffle().toArray(Job.class));

        final JobsHistoryPage ascendingRequest = new JobsHistoryPage(browser).sortByTimestamp(true);

        final Sequence<Pair<Number, JobsHistoryPage.JobHistoryItem>> ascendingPairs = Numbers.range(1, 9).zip(ascendingRequest.items());
        for (Pair<Number, JobsHistoryPage.JobHistoryItem> pair : ascendingPairs) {
            assertThat(pair.second().getTimestamp(), containsString(pair.first().toString()));
        }

        final JobsHistoryPage descendingRequest = ascendingRequest.sortByTimestamp(false);
        final Sequence<Pair<Number, JobsHistoryPage.JobHistoryItem>> descendingPairs = Numbers.range(9, 1).zip(descendingRequest.items());
        for (Pair<Number, JobsHistoryPage.JobHistoryItem> pair : descendingPairs) {
            assertThat(pair.second().getTimestamp(), containsString(pair.first().toString()));
        }
    }

    @Test
    public void resultsCanBeSearchedByJobId() throws Exception {
        final CreatedJob createdJob = jobCreatedAt(new Date());
        final RunningJob runningJob = createdJob.start(new StoppedClock(new Date()));
        jobStorageContains(createdJob, runningJob, jobCreatedAt(new Date()), jobCreatedAt(new Date()));

        final JobsHistoryPage historyPage = new JobsHistoryPage(browser).searchForJobId(createdJob.id());

        assertThat(historyPage.items().size(), is(2));
    }

    @Test
    public void queriesForOldJobHistoryItems() throws Exception {
        Date now = new Date();
        final CreatedJob job1 = jobCreatedAt(Days.subtract(now, 1));
        final CreatedJob job2 = jobCreatedAt(Days.subtract(now, 2));
        final CreatedJob job3 = jobCreatedAt(Days.subtract(now, 3));
        jobStorageContains(job1, job2, job3);

        final JobsHistoryPage historyPage = JobsHistoryPage.find(browser, queryForOlderThan(48));
        final DateFormat dateFormatter = new SimpleDateFormat(DateRenderer.DEFAULT_DATE_FORMAT, Locale.ENGLISH);
        assertThat(sequence(historyPage.items()).map(timestamp).map(trim()), contains(dateFormatter.format(job2.created()), dateFormatter.format(job3.created())));
    }

    @Test
    public void returnsBadRequestResponseIfRemoveQueryIsEmpty() throws Exception {
        assertThat(deleteJobHistoryItemsThatMatch("").status(), is(Status.BAD_REQUEST));
        assertThat(deleteJobHistoryItemsThatMatch("  ").status(), is(Status.BAD_REQUEST));
    }

    @Test
    public void removesNothing() throws Exception {
        jobStorageContains(jobCreatedAt(date(1983, 10, 7)));

        final JobsHistoryPage historyPage = new JobsHistoryPage(browser);
        assertThat(historyPage.items().size(), is(1));

        deleteJobHistoryItemsThatMatch("nonMatchingQuery");
        assertThat(historyPage.items().size(), is(1));
    }

    @Test
    public void removesSomething() throws Exception {
        final CreatedJob createdJob = jobCreatedAt(date(1983, 10, 6));
        jobStorageContains(createdJob);
        jobStorageContains(jobCreatedAt(date(1983, 10, 7)));

        deleteJobHistoryItemsThatMatch(format("jobId:\"%s\"", createdJob.id().toString()));

        final JobsHistoryPage historyPage = new JobsHistoryPage(browser);
        assertThat(historyPage.items().size(), is(1));
    }

    private Response deleteJobHistoryItemsThatMatch(String nonMatchingQuery) throws Exception {
        return application.handle(post(relativeUriOf(method(on(JobsHistoryResource.class).remove(nonMatchingQuery)))).build());
    }

    private Matcher<String> containsLinkFor(final String item) {
        return new TypeSafeMatcher<String>() {

            @Override
            protected boolean matchesSafely(String text) {
                return Pattern.compile(format("<a href=\".*%%22%s%%22\">%s</a>", item, item), Pattern.DOTALL).matcher(text).find();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Contains a link for " + item);
            }
        };
    }

    private RunningJob startedJobStartedAt(Date startTime) {
        return new RunningJob(jobCreatedAt(date(1983, 10, 7)), startTime);
    }

    private CreatedJob jobCreatedAt(Date date) {
        return jobCreatedAt(date, post("/foo").form("id", CRAWLER_UUID).build());
    }

    private CreatedJob jobCreatedAt(Date date, Request request) {
        return CreatedJob.createJob(request, new StoppedClock(date));
    }

    private Job[] jobs(int count) {
        return sequence(jobCreatedAt(new Date())).cycle().take(count).toArray(Job.class);
    }

    private Void jobStorageContains(final Job... job) {
        return application.usingRequestScope(new Block<Container>() {
            @Override
            protected void execute(Container container) throws Exception {
                final JobsStorage repository = container.get(JobsStorage.class);
                sequence(job).forEach(new Block<Job>() {
                    @Override
                    protected void execute(Job job) throws Exception {
                        repository.put(job);
                    }
                });
            }
        });
    }

}