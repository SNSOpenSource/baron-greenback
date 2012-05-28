package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import com.googlecode.utterlyidle.Response;
import org.junit.Test;

import static com.googlecode.barongreenback.crawler.DataExtractor.extractData;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DataExtractorTest {
    @Test
    public void shouldConvertSimpleXml() throws Exception {
        Keyword<String> childName = keyword("name", String.class);
        Response response = response().entity("<root><child><name>bob</name></child><child><name>sue</name></child></root>").build();
        Sequence<Record> records = extractData(response, definition("/root/child", childName));
        assertThat(records.size(), NumberMatcher.is(2));
        assertThat(records.head().get(childName), is("bob"));
        assertThat(records.second().get(childName), is("sue"));
    }

    @Test
    public void shouldIgnoreUnrelatedXml() throws Exception {
        Keyword<String> childName = keyword("name", String.class);
        Response response = response().entity("<someOtherXml/>").build();
        Sequence<Record> records = extractData(response, definition("/root/child", childName));
        assertThat(records.size(), NumberMatcher.is(0));
    }

    @Test
    public void shouldIgnoreEmptyResponse() throws Exception {
        Keyword<String> childName = keyword("name", String.class);
        Response emptyResponse = response().entity("").build();
        Sequence<Record> records = extractData(emptyResponse, definition("/root/child", childName));
        assertThat(records.size(), NumberMatcher.is(0));
    }
}
