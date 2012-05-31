package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.HttpClient;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Xml.selectContents;

public class PaginatedHttpJob extends HttpJob {
    private PaginatedHttpJob(Map<String, Object> context, HttpClient httpHandler, BlockingDeque<Pair<HttpDataSource, Response>> retry) {
        super(context, httpHandler, retry);
    }

    public static PaginatedHttpJob paginatedHttpJob(Map<String, Object> context, HttpClient httpHandler, BlockingDeque<Pair<HttpDataSource, Response>> retry) {
        return new PaginatedHttpJob(context, httpHandler, retry);
    }

    public static PaginatedHttpJob paginatedHttpJob(HttpDataSource dataSource, Definition destination, HttpClient httpHandler, Object checkpoint, String moreXPath, StringMappings mappings, BlockingDeque<Pair<HttpDataSource, Response>> retry) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("dataSource", dataSource);
        context.put("destination", destination);

        context.put("moreXPath", moreXPath);
        context.put("checkpoint", checkpoint);
        context.put("checkpointXPath", checkpointXPath(dataSource.definition()));
        context.put("checkpointAsString", checkpointAsString(mappings, checkpoint));

        return paginatedHttpJob(context, httpHandler, retry);
    }

    public static String checkpointXPath(Definition source) {
        Keyword<?> keyword = source.fields().find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).get();
        return String.format("%s/%s", source.name(), keyword.name());
    }

    @Override
    public Option<PaginatedHttpJob> additionalWork(Definition destination, Document document) {
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
        return paginatedHttpJob(newContext, httpClient, retry);
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

    @Override
    public Function1<Sequence<Record>, Sequence<Record>> filter() {
        return new Function1<Sequence<Record>, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Sequence<Record> records) throws Exception {
                return CheckPointStopper2.stopAt(checkpoint(), records);
            }
        };
    }

}