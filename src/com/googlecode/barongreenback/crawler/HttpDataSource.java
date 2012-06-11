package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Option.none;

public class HttpDataSource {
    protected final Uri uri;
    protected final Definition source;

    public HttpDataSource(Uri uri, Definition source) {
        this.uri = uri;
        this.source = source;
    }

    public static HttpDataSource dataSource(Uri uri, Definition source) {
        return new HttpDataSource(uri, source);
    }

    public HttpDataSource uri(Uri uri) {
        return new HttpDataSource(uri, source);
    }

    public Uri uri() {
        return uri;
    }

    public Request request() {
        return RequestBuilder.get(uri).build();
    }

    public Definition definition() {
        return source;
    }
}
