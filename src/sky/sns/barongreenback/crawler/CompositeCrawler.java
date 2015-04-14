package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.PrintAuditor;

import java.io.PrintStream;
import java.util.Date;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;


public class CompositeCrawler {
    public static final Keyword<String> MORE = keyword("more", String.class);
    public static final Keyword<Boolean> CHECKPOINT = keyword("checkpoint", Boolean.class);
    public static final Keyword<Date> CHECKPOINT_VALUE = keyword("checkpointValue", Date.class);

    private final HttpHandler client;

    public CompositeCrawler() {
        this(new ClientHttpHandler(), System.out);
    }

    public CompositeCrawler(HttpClient client, PrintStream log) {
        this.client = new AuditHandler(client, new PrintAuditor(log));
    }

    public Sequence<Record> crawl(Uri uri, String more, Object checkpoint, RecordDefinition recordDefinition) throws Exception {
        Feeder<Uri> feeder = new SubFeeder(new DuplicateRemover(new CheckpointStopper(checkpoint, new UriFeeder(client, more))));
        return feeder.get(uri, recordDefinition);
    }
}
