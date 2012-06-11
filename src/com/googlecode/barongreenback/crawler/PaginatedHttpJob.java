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

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.totallylazy.Xml.selectContents;

public class PaginatedHttpJob extends HttpJob {
    protected StringMappings mappings;

    protected PaginatedHttpJob(Map<String, Object> context, StringMappings mappings) {
        super(context);
        this.mappings = mappings;
    }

    public static PaginatedHttpJob paginatedHttpJob(Map<String, Object> context, StringMappings mappings) {
        return new PaginatedHttpJob(context, mappings);
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>> process(final Container container) {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob<Response>>> call(Response response) throws Exception {
                return process(loadDocument(response));
            }
        };
    }

    protected Pair<Sequence<Record>, Sequence<StagedJob<Response>>> process(Document document) {
        DocumentProcessor processed = new DocumentProcessor(document, dataSource(), destination(), checkpoint());
        return cast(Pair.pair(processed.merged(), processed.subfeedJobs().join(nextPageJob(document))));
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
        return paginatedHttpJob(newContext, mappings);
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