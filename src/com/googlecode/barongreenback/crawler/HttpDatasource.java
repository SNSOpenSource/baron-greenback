package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;

import java.util.UUID;

public class HttpDatasource {
    protected final Uri uri;
    private final UUID id;
    protected final Definition source;
    private final Record record;

    public HttpDatasource(Uri uri, UUID id, Definition source, Record record) {
        this.uri = uri;
        this.id = id;
        this.source = source;
        this.record = record;
    }

    public static HttpDatasource datasource(Uri uri, UUID id, Definition source) {
        return new HttpDatasource(uri, id, source, Record.constructors.record());
    }

    public static HttpDatasource datasource(Uri uri, UUID id, Definition source, Record record) {
        return new HttpDatasource(uri, id, source, record);
    }

    public HttpDatasource uri(Uri uri) {
        return new HttpDatasource(uri, id, source, record);
    }

    public Uri uri() {
        return uri;
    }

    public Request request() {
        return RequestBuilder.get(uri).build();
    }

    public Definition source() {
        return source;
    }

    public Record record() {
        return record;
    }

    public UUID crawlerId() {
        return id;
    }

    @Override
    public int hashCode() {
        return uri().hashCode() * source().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof HttpDatasource) && ((HttpDatasource) obj).uri().equals(uri) && ((HttpDatasource) obj).source().equals(source);
    }
}
