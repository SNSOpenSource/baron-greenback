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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;

public class MasterPaginatedHttpJob extends PaginatedHttpJob {
    private final StringMappings mappings;

    private MasterPaginatedHttpJob(Map<String, Object> context, StringMappings mappings) {
        super(context);
        context.put("visited", Collections.newSetFromMap(new ConcurrentHashMap<HttpDatasource, Boolean>()));
        this.mappings = mappings;
    }

    public static MasterPaginatedHttpJob masterPaginatedHttpJob(HttpDatasource datasource, Definition destination, Object checkpoint, String moreXPath, StringMappings mappings) {
        return new MasterPaginatedHttpJob(createContext(datasource, destination, checkpoint, moreXPath, mappings), mappings);
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>> process(final Container crawlerScope) {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob>> call(Response response) throws Exception {
                updateView(crawlerScope);

                Option<Document> document = loadDocument(response);

                for (Document doc : document) crawlerScope.get(CheckpointUpdater.class).update(selectCheckpoints(doc).headOption().map(toDateValue()));

                return processDocument(document);
            }

            private void updateView(Container crawlerScope) {
                ViewsRepository viewsRepository = crawlerScope.get(ViewsRepository.class);
                CrawlerRepository crawlerRepository = crawlerScope.get(CrawlerRepository.class);
                Model crawler = crawlerRepository.crawlerFor(datasource().crawlerId());
                viewsRepository.ensureViewForCrawlerExists(crawler, AbstractCrawler.destinationDefinition(crawler).fields());
            }
        };
    }

    private Callable1<String, Date> toDateValue() {
        return new Callable1<String, Date>() {
            @Override
            public Date call(String value) throws Exception {
                return mappings.toValue(Date.class, value);
            }
        };
    }
}