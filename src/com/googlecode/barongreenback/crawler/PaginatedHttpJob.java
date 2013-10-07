package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.Xml;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;
import org.w3c.dom.Document;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.CheckpointStopper.matchesCurrentCheckpoint;
import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.lazyrecords.Keyword.functions.metadata;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.Uri.functions.uri;
import static com.googlecode.totallylazy.Xml.functions.selectContents;
import static com.googlecode.totallylazy.Xml.functions.selectNodes;

public class PaginatedHttpJob extends HttpJob {
    protected PaginatedHttpJob(Model context) {
        super(context);
    }

    static PaginatedHttpJob paginatedHttpJob(Model context) {
        return new PaginatedHttpJob(context);
    }

    public static PaginatedHttpJob paginatedHttpJob(UUID crawlerId, Record record, HttpDatasource datasource, Definition destination, Object checkpoint, String moreXPath, Set<HttpDatasource> visited, Date createdDate) {
        return paginatedHttpJob(createContext(crawlerId, record, datasource, destination, checkpoint, moreXPath, visited, createdDate));
    }

    protected static Model createContext(UUID crawlerId, Record record, HttpDatasource datasource, Definition destination, Object checkpoint, String moreXPath, Set<HttpDatasource> visited, Date createdDate) {
        return createContext(crawlerId, record, datasource, destination, visited, createdDate).
                set("moreXPath", moreXPath).
                set("checkpoint", checkpoint).
                set("checkpointXPath", checkpointXPath(datasource.source()));
    }

    protected static String checkpointXPath(Definition source) {
        Keyword<?> keyword = source.fields().find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).get();
        return String.format("%s/%s", source.name(), keyword.name());
    }

    public Pair<Sequence<Record>, Sequence<StagedJob>> process(final Container crawlerScope, Response response) throws Exception {
        return processDocument(loadDocument(response));
    }

    protected Pair<Sequence<Record>, Sequence<StagedJob>> processDocument(Option<Document> document) {
        Sequence<Record> events = transformData(document, datasource().source());
        Sequence<Record> filtered = CheckpointStopper.stopAt(checkpoint(), events);
        Pair<Sequence<Record>, Sequence<StagedJob>> pair = new SubfeedJobCreator(destination(), visited(), crawlerId(), record(), createdDate()).process(filtered);
        Sequence<StagedJob> sequence = Sequences.<StagedJob>sequence(nextPageJob(document));
        return Pair.pair(pair.first(), sequence.join(pair.second()));
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
        return selectCheckpoints(document).exists(matchesCurrentCheckpoint(checkpoint()));
    }

    private PaginatedHttpJob job(HttpDatasource datasource) {
        return paginatedHttpJob(context.set("datasource", datasource));
    }

    protected Sequence<String> selectCheckpoints(Document document) {
        return one(checkpointXPath()).filter(not(empty())).flatMap(selectNodes().apply(document)).map(Xml.contents());
    }

    protected Object checkpoint() {
        return context.get("checkpoint");
    }

    private String moreXPath() {
        return context.get("moreXPath");
    }

    private String checkpointXPath() {
        return context.get("checkpointXPath");
    }

    @Override
    public String toString() {
        return String.format("%s, checkpoint: %s, moreXPath: %s, checkpointXPath: %s", super.toString(), checkpoint(), moreXPath(), checkpointXPath());
    }
}
