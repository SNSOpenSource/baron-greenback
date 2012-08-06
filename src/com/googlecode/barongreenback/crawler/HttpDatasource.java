package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;

public class HttpDatasource {
    private final Uri uri;
    private final Definition source;

    private HttpDatasource(Uri uri, Definition source) {
        this.uri = uri;
        this.source = source;
    }

    public static HttpDatasource httpDatasource(Uri uri, Definition source) {
        return new HttpDatasource(uri, source);
    }

    public HttpDatasource uri(Uri uri) {
        return httpDatasource(uri, source);
    }

    public Uri uri() {
        return uri;
    }

    public Definition source() {
        return source;
    }

    public Request request() {
        return RequestBuilder.get(uri).build();
    }

    @Override
    public int hashCode() {
        return uri().hashCode() * source().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof HttpDatasource) && ((HttpDatasource) obj).uri().equals(uri) && ((HttpDatasource) obj).source().equals(source);
    }

    @Override
    public String toString() {
        return String.format("uri: %s, source: %s", uri, source);
    }
}