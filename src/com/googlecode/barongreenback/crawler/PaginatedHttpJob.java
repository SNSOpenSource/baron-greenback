package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;
import org.w3c.dom.Document;

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
    private PaginatedHttpJob(Map<String, Object> context) {
        super(context);
    }

    public static PaginatedHttpJob paginatedHttpJob(Map<String, Object> context) {
        return new PaginatedHttpJob(context);
    }

    public static PaginatedHttpJob paginatedHttpJob(HttpDataSource dataSource, Definition destination, Object checkpoint, String moreXPath, StringMappings mappings) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("dataSource", dataSource);
        context.put("destination", destination);

        context.put("moreXPath", moreXPath);
        context.put("checkpoint", checkpoint);
        context.put("checkpointXPath", checkpointXPath(dataSource.definition()));
        context.put("checkpointAsString", checkpointAsString(mappings, checkpoint));

        return paginatedHttpJob(context);
    }

    public static String checkpointXPath(Definition source) {
        Keyword<?> keyword = source.fields().find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).get();
        return String.format("%s/%s", source.name(), keyword.name());
    }

    public Option<PaginatedHttpJob> additionalWork(Document document) {
        if (Strings.isEmpty(moreXPath())) return none();
        Uri moreUri = Uri.uri(selectContents(document, moreXPath()));

        if (!containsCheckpoint(document)) {
            HttpDataSource newDataSource = dataSource().uri(moreUri);
            return Option.some(this.datasource(newDataSource));
        }
        return none();
    }

    private PaginatedHttpJob datasource(HttpDataSource dataSource) {
        ConcurrentHashMap<String, Object> newContext = new ConcurrentHashMap<String, Object>(context);
        newContext.put("dataSource", dataSource);
        return paginatedHttpJob(newContext);
    }

    public boolean containsCheckpoint(Document document) {
        return selectCheckpoints(document).contains(checkpointAsString());
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

    public String moreXPath() {
        return (String) context.get("moreXPath");
    }

    public static String checkpointAsString(StringMappings mappings, Object checkpoint) {
        if (checkpoint == null) return null;
        return mappings.toString(checkpoint.getClass(), checkpoint);
    }

    public Function1<Sequence<Record>, Sequence<Record>> filter() {
        return new Function1<Sequence<Record>, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Sequence<Record> records) throws Exception {
                return CheckPointStopper2.stopAt(checkpoint(), records);
            }
        };
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>> process() {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob<Response>>> call(Response response) throws Exception {
                Document document = loadDocument(response);
                Option<PaginatedHttpJob> jobs = additionalWork(document);
                Sequence<Record> records = transformData(document, dataSource().definition());
                Sequence<Record> filtered = filter().apply(records);
                Sequence<HttpJob> moreJobs = Subfeeder2.subfeeds(filtered, destination());
                Sequence<Record> merged = Subfeeder2.mergePreviousUniqueIdentifiers(filtered, dataSource());
                return cast(Pair.pair(merged, moreJobs.join(jobs)));
            }
        };
    }


}