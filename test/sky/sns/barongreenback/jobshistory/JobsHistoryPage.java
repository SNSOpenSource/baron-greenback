package com.googlecode.barongreenback.jobshistory;

import com.googlecode.barongreenback.shared.pager.Pager;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;
import com.googlecode.utterlyidle.html.Table;
import com.googlecode.utterlyidle.html.TableCell;
import com.googlecode.utterlyidle.html.TableRow;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.googlecode.barongreenback.shared.sorter.Sorter.ASCENDING_SORT_DIRECTION;
import static com.googlecode.barongreenback.shared.sorter.Sorter.DESCENDING_SORT_DIRECTION;
import static com.googlecode.barongreenback.shared.sorter.Sorter.SORT_COLUMN_QUERY_PARAM;
import static com.googlecode.barongreenback.shared.sorter.Sorter.SORT_DIRECTION_QUERY_PARAM;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class JobsHistoryPage {
    private final Html html;
    private HttpHandler httpHandler;

    public static JobsHistoryPage find(HttpHandler httpHandler, String query) throws Exception {
        return new JobsHistoryPage(httpHandler, httpHandler.handle(get(relativeUriOf(method(on(JobsHistoryResource.class).list(query)))).build()));
    }

    public JobsHistoryPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(JobsHistoryResource.class).list("")))).build()));
    }

    public JobsHistoryPage(HttpHandler httpHandler, int pageSize) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(JobsHistoryResource.class).list("")))).query(Pager.ROWS_PER_PAGE_PARAM, pageSize).build()));
    }

    public JobsHistoryPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Jobs History"));
    }

    public List<JobHistoryItem> items() {
        final Table table = html.table("//table[contains(@class, 'results')]");
        return table.bodyRows().map(asJobHistoryItem()).toList();
    }

    private static Function1<? super TableRow, JobHistoryItem> asJobHistoryItem() {
        return new Function1<TableRow, JobHistoryItem>() {
            @Override
            public JobHistoryItem call(TableRow tableRow) throws Exception {
                final Sequence<TableCell> cells = tableRow.cells();
                final String shortMessage = cells.get(4).selectContent("//div[@class='shortMessage']/text()").trim();
                final String rawMessage = cells.get(4).selectContent("//div[@class='rawMessage']/text()").replace("\n", "\r\n");
                return new JobHistoryItem(decodeCell(cells.get(0)), decodeCell(cells.get(1)), decodeCell(cells.get(2)), decodeCell(cells.get(3)), shortMessage, rawMessage);
            }
        };
    }

    private static String decodeCell(TableCell tableCell) {
        final String value = tableCell.toString();
        final Matcher matcher = Pattern.compile("<td.*?>(.*)</td>", Pattern.DOTALL).matcher(value);
        matcher.find();
        return matcher.group(1);
    }

    public JobsHistoryPage page(int pageNumber) throws Exception {
        return new JobsHistoryPage(this.httpHandler, this.httpHandler.handle(this.html.link("//div[@class='pagination']/ul/li[contains(@class, 'page')]/a[text()='" + pageNumber + "']").click()));
    }

    public JobsHistoryPage sortByTimestamp(boolean ascending) throws Exception {
        return new JobsHistoryPage(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(JobsHistoryResource.class).list(""))))
                .query(SORT_COLUMN_QUERY_PARAM, "timestamp")
                .query(SORT_DIRECTION_QUERY_PARAM,
                        ascending
                                ? ASCENDING_SORT_DIRECTION
                                : DESCENDING_SORT_DIRECTION
                ).build()));
    }

    public JobsHistoryPage searchForJobId(UUID id) throws Exception {
        return new JobsHistoryPage(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(JobsHistoryResource.class).list(String.format("jobId:\"%s\"", id))))).build()));
    }

    public static class JobHistoryItem {
        private String timestamp;
        private String jobId;
        private String action;
        private String shortMessage;
        private String rawMessage;
        private String elapsedTime;
        public JobHistoryItem(String timestamp, String elapsedTime, String action, String jobId, String shortMessage, String rawMessage) {
            this.timestamp = timestamp;
            this.jobId = jobId;
            this.action = action;
            this.shortMessage = shortMessage;
            this.rawMessage = rawMessage;
            this.elapsedTime = elapsedTime;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getJobId() {
            return jobId;
        }

        public String getAction() {
            return action;
        }

        public String getShortMessage() {
            return shortMessage;
        }

        public String getRawMessage() {
            return rawMessage;
        }

        public String getElapsedTime() {
            return elapsedTime;
        }

        public static class functions {
            public static Function1<JobHistoryItem,String> timestamp = new Function1<JobHistoryItem, String>() {
                @Override
                public String call(JobHistoryItem jobHistoryItem) throws Exception {
                    return jobHistoryItem.getTimestamp();
                }
            };
        }
    }
}
