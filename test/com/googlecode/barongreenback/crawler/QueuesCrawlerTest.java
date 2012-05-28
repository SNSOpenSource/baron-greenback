package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import com.googlecode.utterlyidle.*;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.utterlyidle.Requests.request;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class QueuesCrawlerTest {
    @Test
    public void shouldConvertSimpleXml() throws Exception {
        ImmutableKeyword<String> childName = Keywords.keyword("name", String.class);
        Function1<Response, Sequence<Record>> extractor = QueuesCrawler.simpleExtractData(Definition.constructors.definition("/root/child", childName));
        Sequence<Record> records = extractor.apply(ResponseBuilder.response().entity("<root><child><name>bob</name></child><child><name>sue</name></child></root>").build());
        assertThat(records.size(), NumberMatcher.is(2));
        assertThat(records.head().get(childName), is("bob"));
        assertThat(records.second().get(childName), is("sue"));
    }

    @Test
    public void shouldIgnoreUnrelatedXml() throws Exception {
        ImmutableKeyword<String> childName = Keywords.keyword("name", String.class);
        Function1<Response, Sequence<Record>> extractor = QueuesCrawler.simpleExtractData(Definition.constructors.definition("/root/child", childName));
        Sequence<Record> records = extractor.apply(ResponseBuilder.response().entity("<someOtherXml/>").build());
        assertThat(records.size(), NumberMatcher.is(0));
    }

    @Test
    public void shouldIgnoreEmptyResponse() throws Exception {
        ImmutableKeyword<String> childName = Keywords.keyword("name", String.class);
        Function1<Response, Sequence<Record>> extractor = QueuesCrawler.simpleExtractData(Definition.constructors.definition("/root/child", childName));
        Sequence<Record> records = extractor.apply(ResponseBuilder.response().entity("").build());
        assertThat(records.size(), NumberMatcher.is(0));
    }

    @Test
    public void shouldPlaceOnRetryQueueIfFailed() throws Exception {
        LinkedBlockingDeque<Pair<Request, Response>> retryQueue = new LinkedBlockingDeque<Pair<Request, Response>>();
        Request request = RequestBuilder.get("/any/uri").build();
        Function1<Response, Response> failer = QueuesCrawler.queueIfFailed(request, retryQueue);
        Response originalResponse = ResponseBuilder.response(Status.NOT_FOUND).build();
        Response response = failer.apply(originalResponse);
        assertThat(response.entity().toString(), is(""));
        assertThat(response.status(), is(Status.NO_CONTENT));
        assertThat(retryQueue.contains(pair(request, originalResponse)), is(true));
    }

    @Test
    public void shouldReturnOriginalResponseWhenOk() throws Exception {
        LinkedBlockingDeque<Pair<Request, Response>> retryQueue = new LinkedBlockingDeque<Pair<Request, Response>>();
        Function1<Response, Response> failer = QueuesCrawler.queueIfFailed(RequestBuilder.get("/any/uri").build(), retryQueue);
        Response expectedResponse = ResponseBuilder.response(Status.OK).build();
        Response response = failer.apply(expectedResponse);
        assertThat(response, is(expectedResponse));
        assertThat(retryQueue.size(), is(0));
    }
}
