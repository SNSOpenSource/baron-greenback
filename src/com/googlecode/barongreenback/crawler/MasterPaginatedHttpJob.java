package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;
import org.w3c.dom.Document;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;

public class MasterPaginatedHttpJob extends PaginatedHttpJob {
    private MasterPaginatedHttpJob(Map<String, Object> context) {
        super(context);
    }

    public static MasterPaginatedHttpJob masterPaginatedHttpJob(UUID crawlerId, HttpDatasource datasource, Definition destination, Object checkpoint, String moreXPath, StringMappings mappings, VisitedFactory visitedFactory) {
        return new MasterPaginatedHttpJob(createContext(crawlerId, Record.constructors.record(), datasource, destination, checkpoint, moreXPath, mappings, visitedFactory.value()));
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>> process(final Container crawlerScope) {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob>> call(Response response) throws Exception {
                updateView(crawlerScope);

                Option<Document> document = loadDocument(response);

                for (Document doc : document)
                    crawlerScope.get(CheckpointUpdater.class).update(selectCheckpoints(doc).headOption().map(toDateValue(crawlerScope.get(StringMappings.class))));

                return processDocument(document);
            }

            private void updateView(Container crawlerScope) {
                ViewsRepository viewsRepository = crawlerScope.get(ViewsRepository.class);
                CrawlerRepository crawlerRepository = crawlerScope.get(CrawlerRepository.class);
                Model crawler = crawlerRepository.crawlerFor(crawlerId());
                viewsRepository.ensureViewForCrawlerExists(crawler, AbstractCrawler.destinationDefinition(crawler).fields());
            }
        };
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