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
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.totallylazy.Xml.selectContents;

public class PaginatedHttpJob extends HttpJob {
    private static StringMappings mappings;
    private final boolean master;

    private PaginatedHttpJob(Map<String, Object> context, boolean master) {
        super(context);
        this.master = master;
    }

    public static PaginatedHttpJob paginatedHttpJob(Map<String, Object> context) {
        return new PaginatedHttpJob(context, false);
    }

    public static PaginatedHttpJob paginatedHttpJob(HttpDataSource dataSource, Definition destination, Object checkpoint, String moreXPath, StringMappings mappings) {
        PaginatedHttpJob.mappings = mappings;
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("dataSource", dataSource);
        context.put("destination", destination);

        context.put("moreXPath", moreXPath);
        context.put("checkpoint", checkpoint);
        context.put("checkpointXPath", checkpointXPath(dataSource.definition()));
        context.put("checkpointAsString", checkpointAsString(mappings, checkpoint));

        return new PaginatedHttpJob(context, true);
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>> process(final Container container) {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob<Response>>> call(Response response) throws Exception {
                Document document = loadDocument(response);
                if(master) {
                    final CheckpointUpdater checkpointUpdater = container.get(CheckpointUpdater.class);
                    final Date checkpoint = mappings.toValue(Date.class, selectCheckpoints(document).head());
                    checkpointUpdater.update(Option.<Object>some(checkpoint));
                }
                Option<PaginatedHttpJob> nextPageJob = additionalWork(document);
                Sequence<Record> records = transformData(document, dataSource().definition());
                Sequence<Record> filtered = CheckPointStopper2.stopAt(checkpoint(), records);
                Sequence<HttpJob> subfeedJobs = Subfeeder2.subfeeds(filtered, destination());
                Sequence<Record> merged = Subfeeder2.mergePreviousUniqueIdentifiers(filtered, dataSource());
                return cast(Pair.pair(merged, subfeedJobs.join(nextPageJob)));
            }
        };
    }

    public Option<PaginatedHttpJob> additionalWork(Document document) {
        if (Strings.isEmpty(moreXPath())) return none();
        Uri moreUri = Uri.uri(selectContents(document, moreXPath()));

        if (!containsCheckpoint(document)) {
            return Option.some(datasource(dataSource().uri(moreUri)));
        }
        return none();
    }

    private boolean containsCheckpoint(Document document) {
        return selectCheckpoints(document).contains(checkpointAsString());
    }

    private String moreXPath() {
        return (String) context.get("moreXPath");
    }

    private static String checkpointXPath(Definition source) {
        Keyword<?> keyword = source.fields().find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).get();
        return String.format("%s/%s", source.name(), keyword.name());
    }

    private static String checkpointAsString(StringMappings mappings, Object checkpoint) {
        if (checkpoint == null) return null;
        return mappings.toString(checkpoint.getClass(), checkpoint);
    }

    private PaginatedHttpJob datasource(HttpDataSource dataSource) {
        ConcurrentHashMap<String, Object> newContext = new ConcurrentHashMap<String, Object>(context);
        newContext.put("dataSource", dataSource);
        return paginatedHttpJob(newContext);
    }

    private Sequence<String> selectCheckpoints(Document document) {
        return Xml.selectNodes(document, checkpointXPath()).map(Xml.contents());
    }

    private Object checkpoint() {
        return context.get("checkpoint");
    }

    private String checkpointXPath() {
        return (String) context.get("checkpointXPath");
    }

    private String checkpointAsString() {
        return (String) context.get("checkpointAsString");
    }
}