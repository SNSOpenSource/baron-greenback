package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;
import org.w3c.dom.Document;

import java.util.Date;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;

public class MasterPaginatedHttpJob extends PaginatedHttpJob {
    private MasterPaginatedHttpJob(Model context) {
        super(context);
    }

    public static MasterPaginatedHttpJob masterPaginatedHttpJob(UUID crawlerId, HttpDatasource datasource, Definition destination, Object checkpoint, String moreXPath, VisitedFactory visitedFactory, Clock clock) {
        return masterPaginatedHttpJob(createContext(crawlerId, Record.constructors.record(), datasource, destination, checkpoint, moreXPath, visitedFactory.value(), clock.now()));
    }

    public static MasterPaginatedHttpJob masterPaginatedHttpJob(Model model) {
        return new MasterPaginatedHttpJob(model);
    }

    public Pair<Sequence<Record>, Sequence<StagedJob>> process(final Container crawlerScope, Response response) throws Exception {
        Option<Document> document = loadDocument(response);
        for (Document doc : document)
            crawlerScope.get(CheckpointUpdater.class).update(selectCheckpoints(doc).headOption().map(toDateValue(crawlerScope.get(BaronGreenbackStringMappings.class).value())));
        return processDocument(document);
    }

    private Callable1<String, Date> toDateValue(final StringMappings mappings) {
        return new Callable1<String, Date>() {
            @Override
            public Date call(String value) throws Exception {
                return mappings.toValue(Date.class, value);
            }
        };
    }
}