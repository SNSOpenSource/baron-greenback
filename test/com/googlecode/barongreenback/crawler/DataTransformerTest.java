package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import com.googlecode.utterlyidle.Response;
import org.junit.Test;
import org.w3c.dom.Document;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.totallylazy.Xml.document;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DataTransformerTest {
    @Test
    public void shouldConvertSimpleXml() throws Exception {
        Keyword<String> childName = keyword("name", String.class);
        Document document = document("<root><child><name>bob</name></child><child><name>sue</name></child></root>");
        Sequence<Record> records = DataTransformer.transformData(Option.some(document), definition("/root/child", childName));
        assertThat(records.size(), NumberMatcher.is(2));
        assertThat(records.head().get(childName), is("bob"));
        assertThat(records.second().get(childName), is("sue"));
    }

    @Test
    public void shouldIgnoreUnrelatedXml() throws Exception {
        Keyword<String> childName = keyword("name", String.class);
        Document document = document("<someOtherXml/>");
        Sequence<Record> records = DataTransformer.transformData(Option.some(document), definition("/root/child", childName));
        assertThat(records.size(), NumberMatcher.is(0));
    }

    @Test
    public void shouldCreateEmptyDocumentForEmptyResponse() throws Exception {
        Response emptyResponse = response().entity("").build();
        Option<Document> document = DataTransformer.loadDocument(emptyResponse);
        assertThat(document.isEmpty(), is(true));
    }
}
