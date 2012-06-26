package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.CountLatch;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class MasterPaginatedHttpJob extends PaginatedHttpJob {

    private final CheckpointUpdater checkpointUpdater;

    private MasterPaginatedHttpJob(Container container, Map<String, Object> context, StringMappings mappings, CheckpointUpdater checkpointUpdater) {
        super(container, context, mappings);
        this.checkpointUpdater = checkpointUpdater;
    }


    public static MasterPaginatedHttpJob masterPaginatedHttpJob(Container container, HttpDataSource dataSource, Definition destination, Object checkpoint, String moreXPath, StringMappings mappings, CheckpointUpdater checkpointUpdater) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("dataSource", dataSource);
        context.put("destination", destination);

        context.put("moreXPath", moreXPath);
        context.put("checkpoint", checkpoint);
        context.put("checkpointXPath", checkpointXPath(dataSource.definition()));
        context.put("checkpointAsString", checkpointAsString(mappings, checkpoint));

        return new MasterPaginatedHttpJob(container, context, mappings, checkpointUpdater);
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>> process() {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob<Response>>> call(Response response) throws Exception {
                Document document = loadDocument(response);
                checkpointUpdater.update(
                        selectCheckpoints(document).headOption().map(toDateValue())
                );
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