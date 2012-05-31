package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.handlers.HttpClient;
import org.w3c.dom.Document;

import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Xml.selectContents;

public class PaginatedHttpJob extends HttpJob {
    private final String moreXPath;
    private final Object checkpoint;
    private final String checkpointAsString;
    private final String checkpointXPath;

    private PaginatedHttpJob(HttpDataSource dataSource, Definition destination, HttpClient httpHandler, String moreXPath, Object checkpoint, String checkpointAsString, String checkpointXPath) {
        super(dataSource, destination, httpHandler);
        this.moreXPath = moreXPath;
        this.checkpoint = checkpoint;
        this.checkpointAsString = checkpointAsString;
        this.checkpointXPath = checkpointXPath;
    }

    public static PaginatedHttpJob paginatedHttpJob(HttpDataSource dataSource, Definition destination, HttpClient httpHandler, Object checkpoint, String moreXPath, String checkpointAsString, String checkpointXPath) {
        return new PaginatedHttpJob(dataSource, destination, httpHandler, moreXPath, checkpoint, checkpointAsString, checkpointXPath);
    }

    public static PaginatedHttpJob paginatedHttpJob(HttpDataSource dataSource, Definition destination, HttpClient httpHandler, Object checkpoint, String moreXPath, StringMappings mappings) {
        return new PaginatedHttpJob(dataSource, destination, httpHandler, moreXPath, checkpoint, checkpointAsString(mappings, checkpoint), checkpointXPath(dataSource.definition()));
    }

    public static String checkpointXPath(Definition source) {
        Keyword<?> keyword = source.fields().find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).get();
        return String.format("%s/%s", source.name(), keyword.name());
    }

    @Override
    public Option<PaginatedHttpJob> additionalWork(Definition destination, Document document) {
        if(Strings.isEmpty(moreXPath)) return none();
        Uri moreUri = Uri.uri(selectContents(document, moreXPath()));

        if (!containsCheckpoint(document)) {
            HttpDataSource newDataSource = dataSource.uri(moreUri);
            return Option.some(this.datasource(newDataSource));
        }
        return none();
    }

    private PaginatedHttpJob datasource(HttpDataSource dataSource) {
        return paginatedHttpJob(dataSource, destination(), httpHandler, checkpoint, moreXPath, checkpointAsString, checkpointXPath);
    }

    public boolean containsCheckpoint(Document document) {
        return selectCheckpoints(document).contains(checkpointAsString);
    }

    private Sequence<String> selectCheckpoints(Document document) {
        return Xml.selectNodes(document, checkpointXPath).map(Xml.contents());
    }

    public String moreXPath() {
        return moreXPath;
    }

    public static String checkpointAsString(StringMappings mappings, Object checkpoint) {
        if(checkpoint==null) return null;
        return mappings.toString(checkpoint.getClass(), checkpoint);
    }

    @Override
    public Function1<Sequence<Record>, Sequence<Record>> filter() {
        return new Function1<Sequence<Record>, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Sequence<Record> records) throws Exception {
                return  CheckPointStopper2.stopAt(checkpoint, records);
            }
        };
    }

}