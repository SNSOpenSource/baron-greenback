package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.barongreenback.crawler.CheckPointStopper.checkpointReached;
import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Unchecked.cast;

public class MasterPaginatedHttpJob extends PaginatedHttpJob {

    private MasterPaginatedHttpJob(Map<String, Object> context, StringMappings mappings) {
        super(context, mappings);
    }

    public static MasterPaginatedHttpJob masterPaginatedHttpJob(HttpDataSource dataSource, Definition destination, Object checkpoint, String moreXPath, StringMappings mappings) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("dataSource", dataSource);
        context.put("destination", destination);

        context.put("moreXPath", moreXPath);
        context.put("checkpoint", checkpoint);
        context.put("checkpointXPath", checkpointXPath(dataSource.definition()));
        context.put("checkpointAsString", checkpointAsString(mappings, checkpoint));

        return new MasterPaginatedHttpJob(context, mappings);
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>> process(final Container container) {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob<Response>>> call(Response response) throws Exception {
                Document document = loadDocument(response);
                container.get(CheckpointUpdater.class).update(
                        selectCheckpoints(document).headOption().map(toDateValue())
                );
                DocumentProcessor processed = new DocumentProcessor(loadDocument(response), dataSource(), destination(), checkpoint()).execute();
                return cast(Pair.pair(processed.merged(), processed.subfeedJobs().join(nextPageJob(document))));
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