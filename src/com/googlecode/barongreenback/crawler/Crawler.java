package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.PrintAuditor;

import java.util.Date;

import static com.googlecode.lazyrecords.Keywords.keyword;


public class Crawler {
    public static final Keyword<String> MORE = keyword("more", String.class);
    public static final Keyword<Boolean> CHECKPOINT = Keywords.keyword("checkpoint", Boolean.class);
    public static final Keyword<Date> CHECKPOINT_VALUE = keyword("checkpointValue", Date.class);

    private final HttpHandler client;

    public Crawler() {
        this(new ClientHttpHandler());
    }

    public Crawler(HttpClient client) {
        this.client = new AuditHandler(client, new PrintAuditor(System.out));
    }

    public Sequence<Record> crawl(Uri uri, String more, Object checkpoint, RecordDefinition recordDefinition) throws Exception {
        Feeder<Uri> feeder = new SubFeeder(new DuplicateRemover(new CheckPointStopper(checkpoint, new UriFeeder(client, more))));
        return feeder.get(uri, recordDefinition).
                memorise();
    }
}
