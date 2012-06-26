package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;
import org.w3c.dom.Document;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.barongreenback.crawler.CheckPointStopper.checkpointReached;
import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Xml.selectContents;

public class PaginatedHttpJob extends HttpJob {
    protected StringMappings mappings;

    protected PaginatedHttpJob(Container container, Map<String, Object> context, StringMappings mappings) {
        super(container, context);
        this.mappings = mappings;
    }

    public static PaginatedHttpJob paginatedHttpJob(Container container, Map<String, Object> context, StringMappings mappings) {
        return new PaginatedHttpJob(container, context, mappings);
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>> process() {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob<Response>>> call(Response response) throws Exception {
                return processDocument(loadDocument(response), container());
            }
        };
    }

    protected Pair<Sequence<Record>, Sequence<StagedJob<Response>>> processDocument(Document document, Container container) {
        Pair<Sequence<Record>, Sequence<StagedJob<Response>>> pair = SubfeedJobCreator.process(container, dataSource(), destination(), filterToCheckpoint(transformData(document, dataSource().definition())));
        return Pair.pair(pair.first(), pair.second().join(nextPageJob(document)));
    }

    private Sequence<Record> filterToCheckpoint(Sequence<Record> records) {
        return records.takeWhile(Predicates.<Record>not(checkpointReached(checkpoint()))).realise();
    }

    public Callable1<String, Date> toDateValue() {
        return new Callable1<String, Date>() {
            @Override
            public Date call(String value) throws Exception {
                return mappings.toValue(Date.class, value);
            }
        };
    }

    public Option<PaginatedHttpJob> nextPageJob(Document document) {
        if (Strings.isEmpty(moreXPath())) return none();
        Uri moreUri = Uri.uri(selectContents(document, moreXPath()));

        if (!containsCheckpoint(document)) {
            return Option.some(job(dataSource().uri(moreUri)));
        }
        return none();
    }

    private boolean containsCheckpoint(Document document) {
        return selectCheckpoints(document).contains(checkpointAsString());
    }

    private PaginatedHttpJob job(HttpDataSource dataSource) {
        ConcurrentHashMap<String, Object> newContext = new ConcurrentHashMap<String, Object>(context);
        newContext.put("dataSource", dataSource);
        return paginatedHttpJob(container(), newContext, mappings);
    }

    protected Sequence<String> selectCheckpoints(Document document) {
        return Xml.selectNodes(document, checkpointXPath()).map(Xml.contents());
    }

    protected Object checkpoint() {
        return context.get("checkpoint");
    }

    private String moreXPath() {
        return (String) context.get("moreXPath");
    }

    private String checkpointXPath() {
        return (String) context.get("checkpointXPath");
    }

    private String checkpointAsString() {
        return (String) context.get("checkpointAsString");
    }

}