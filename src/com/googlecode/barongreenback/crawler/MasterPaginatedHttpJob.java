package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
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
import java.util.HashMap;
import java.util.Map;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class MasterPaginatedHttpJob extends PaginatedHttpJob {
    private MasterPaginatedHttpJob(Map<String, Object> context, StringMappings mappings) {
        super(context, mappings);
    }

    public static MasterPaginatedHttpJob masterPaginatedHttpJob(HttpDatasource datasource, Definition destination, Object checkpoint, String moreXPath, StringMappings mappings) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("datasource", datasource);
        context.put("destination", destination);

        context.put("moreXPath", moreXPath);
        context.put("checkpoint", checkpoint);
        context.put("checkpointXPath", checkpointXPath(datasource.source()));
        context.put("checkpointAsString", checkpointAsString(mappings, checkpoint));

        return new MasterPaginatedHttpJob(context, mappings);
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>> process(final Container crawlerScope) {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob>> call(Response response) throws Exception {
                updateView(crawlerScope);

                Option<Document> document = loadDocument(response);

                for (Document doc : document) {
                    crawlerScope.get(CheckpointUpdater.class).update(selectCheckpoints(doc).headOption().map(toDateValue()));
                }

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

    private static String checkpointXPath(Definition source) {
        Keyword<?> keyword = source.fields().find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).get();
        return String.format("%s/%s", source.name(), keyword.name());
    }

    private static String checkpointAsString(StringMappings mappings, Object checkpoint) {
        if (checkpoint == null) return null;
        return mappings.toString(checkpoint.getClass(), checkpoint);
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