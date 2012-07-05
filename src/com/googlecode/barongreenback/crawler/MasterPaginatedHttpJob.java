package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
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

    private MasterPaginatedHttpJob(Container container, Map<String, Object> context, StringMappings mappings) {
        super(container, context, mappings);
    }


    public static MasterPaginatedHttpJob masterPaginatedHttpJob(Container crawlContainer, HttpDatasource datasource,
                                                                Definition destination, Object checkpoint, String moreXPath, StringMappings mappings) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("dataSource", datasource);
        context.put("destination", destination);

        context.put("moreXPath", moreXPath);
        context.put("checkpoint", checkpoint);
        context.put("checkpointXPath", checkpointXPath(datasource.source()));
        context.put("checkpointAsString", checkpointAsString(mappings, checkpoint));

        return new MasterPaginatedHttpJob(crawlContainer, context, mappings);
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>> process() {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob>> call(Response response) throws Exception {
                Option<Document> document = loadDocument(response);

                for (Document doc : document) {
                    container().get(CheckpointUpdater.class).update(selectCheckpoints(doc).headOption().map(toDateValue()));
                }

                return processDocument(document, container());
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
}