package com.googlecode.barongreenback.crawler.datasources;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Uri;

public class HttpDataSource implements DataSource {
    private final Uri uri;
    private final Definition source;

    private HttpDataSource(Uri uri, Definition source) {
        this.uri = uri;
        this.source = source;
    }

    public static HttpDataSource httpDataSource(Uri uri, Definition source) {
        return new HttpDataSource(uri, source);
    }

    @Override
    public Uri uri() {
        return uri;
    }

    @Override
    public Definition source() {
        return source;
    }

    @Override
    public int hashCode() {
        return uri().hashCode() * source().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof HttpDataSource) && ((HttpDataSource) obj).uri().equals(uri) && ((HttpDataSource) obj).source().equals(source);
    }

    @Override
    public String toString() {
        return String.format("uri: %s, source: %s", uri, source);
    }
}