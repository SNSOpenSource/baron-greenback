package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.Xml;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;
import org.w3c.dom.Document;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.Uri.functions.uri;
import static com.googlecode.totallylazy.Xml.functions.selectContents;
import static com.googlecode.totallylazy.Xml.functions.selectNodes;

public class PaginatedHttpJob extends HttpJob {
    protected StringMappings mappings;

    protected PaginatedHttpJob(Map<String, Object> context, StringMappings mappings) {
        super(context);
        this.mappings = mappings;
    }

    public static PaginatedHttpJob paginatedHttpJob(Map<String, Object> context, StringMappings mappings) {
        return new PaginatedHttpJob(context, mappings);
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>> process(final Container crawlerScope) {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob>> call(Response response) throws Exception {
                return processDocument(loadDocument(response), crawlerScope);
            }
        };
    }

    protected Pair<Sequence<Record>, Sequence<StagedJob>> processDocument(Option<Document> document, Container container) {
        Sequence<Record> events = transformData(document, datasource().source());
        Sequence<Record> filtered = CheckPointStopper.stopAt(checkpoint(), events);
        Pair<Sequence<Record>, Sequence<StagedJob>> pair = new SubfeedJobCreator(datasource(), destination()).process(filtered);
        return Pair.pair(pair.first(), pair.second().join(nextPageJob(document)));
    }

    public Callable1<String, Date> toDateValue() {
        return new Callable1<String, Date>() {
            @Override
            public Date call(String value) throws Exception {
                return mappings.toValue(Date.class, value);
            }
        };
    }

    public Option<PaginatedHttpJob> nextPageJob(Option<Document> document) {
        return document.flatMap(new Callable1<Document, Option<PaginatedHttpJob>>() {
            @Override
            public Option<PaginatedHttpJob> call(Document document) throws Exception {
                if (containsCheckpoint(document)) return none();
                return moreUri(document).map(toJob());
            }
        });
    }

    private Callable1<Uri, PaginatedHttpJob> toJob() {
        return new Callable1<Uri, PaginatedHttpJob>() {
            @Override
            public PaginatedHttpJob call(Uri uri) throws Exception {
                return job(datasource().uri(uri));
            }
        };
    }

    private Option<Uri> moreUri(final Document document) {
        return one(moreXPath()).filter(not(empty())).map(selectContents().apply(document)).filter(not(empty())).map(uri()).headOption();
    }

    private boolean containsCheckpoint(Document document) {
        return selectCheckpoints(document).contains(checkpointAsString());
    }

    private PaginatedHttpJob job(HttpDatasource datasource) {
        ConcurrentHashMap<String, Object> newContext = new ConcurrentHashMap<String, Object>(context);
        newContext.put("datasource", datasource);
        return paginatedHttpJob(newContext, mappings);
    }

    protected Sequence<String> selectCheckpoints(Document document) {
        return one(checkpointXPath()).filter(not(empty())).flatMap(selectNodes().apply(document)).map(Xml.contents());
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