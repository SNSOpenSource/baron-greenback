package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
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

    public Request request() {
        return RequestBuilder.get(uri).build();
    }

    public Uri uri() {
        return uri;
    }

    public Definition definition() {
        return source;
    }

    public HttpDataSource request(Uri uri) {
        return new HttpDataSource(uri, source);
    }

    public Function1<Document, Iterable<Job>> additionalWork(Definition destination) {
        return new Function1<Document, Iterable<Job>>() {
            @Override
            public Iterable<Job> call(Document document) throws Exception {
                return none();
            }
        };
    }

    public Function1<Sequence<Record>, Sequence<Record>> filter() {
        return Function1.identity();
    }
}
