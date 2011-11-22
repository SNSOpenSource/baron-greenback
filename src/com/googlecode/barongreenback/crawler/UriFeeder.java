package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.Xml.document;

public class UriFeeder implements Feeder<Uri> {
    private final HttpHandler httpClient;
    private final Feeder<Document> feeder;

    public UriFeeder(final HttpHandler handler, String moreXpath) {
        this.httpClient = handler;
        this.feeder = new MoreDocumentFeeder(this, moreXpath);
    }

    public Sequence<Record> get(Uri uri, RecordDefinition definition) throws Exception {
        Response response = httpClient.handle(RequestBuilder.get(uri).build());
        Document document = document(new String(response.bytes()));
        return feeder.get(document, definition);
    }
}
