package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import org.w3c.dom.Document;

import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Xml.selectContents;

public class PaginatedHttpDataSource extends HttpDataSource {
    private final Object checkpoint;
    private final String moreXPath;
    private final String checkpointAsString;
    private final String checkpointXPath;

    private PaginatedHttpDataSource(Uri uri, Definition source, Object checkpoint, String moreXPath, String checkpointAsString, String checkpointXPath) {
        super(uri, source);
        this.checkpoint = checkpoint;
        this.moreXPath = moreXPath;
        this.checkpointAsString = checkpointAsString;
        this.checkpointXPath = checkpointXPath;
    }

    public static PaginatedHttpDataSource dataSource(Uri uri, Definition source, Object checkpoint, String moreXPath, StringMappings mappings) {
        return dataSource(uri, source, checkpoint, moreXPath, checkpointAsString(mappings, checkpoint), checkpointXPath(source));
    }

    public static PaginatedHttpDataSource dataSource(Uri uri, Definition source, Object checkpoint, String moreXPath, String checkpointAsString, String checkpointXPath) {
        return new PaginatedHttpDataSource(uri, source, checkpoint, moreXPath, checkpointAsString, checkpointXPath);
    }

    public Option<Job> getMoreIfNeeded(Document document, Definition destination) {
        Uri moreUri = Uri.uri(selectContents(document, moreXPath()));

        if (!containsCheckpoint(document)) {
            PaginatedHttpDataSource newDataSource = request(moreUri);
            return Option.some(Job.job(newDataSource, destination));
        }
        return none();
    }

    public String moreXPath() {
        return moreXPath;
    }

    public static String checkpointXPath(Definition source) {
        Keyword<?> keyword = source.fields().find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).get();
        return String.format("%s/%s", source.name(), keyword.name());
    }

    public static String checkpointAsString(StringMappings mappings, Object checkpoint) {
        if(checkpoint==null) return null;
        return mappings.toString(checkpoint.getClass(), checkpoint);
    }

    public boolean containsCheckpoint(Document document) {
        return selectCheckpoints(document).contains(checkpointAsString);
    }

    private Sequence<String> selectCheckpoints(Document document) {
        return Xml.selectNodes(document, checkpointXPath).map(Xml.contents());
    }

    public PaginatedHttpDataSource request(Uri uri) {
        return dataSource(uri, source, checkpoint, moreXPath, checkpointAsString, checkpointXPath);
    }

    public Function1<Document, Iterable<Job>> additionalWork(final Definition destination) {
        return new Function1<Document, Iterable<Job>>() {
            @Override
            public Iterable<Job> call(Document document) throws Exception {
                return getMoreIfNeeded(document, destination);
            }
        };
    }

    public Function1<Sequence<Record>, Sequence<Record>> filter() {
        return new Function1<Sequence<Record>, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Sequence<Record> records) throws Exception {
                return  CheckPointStopper2.stopAt(checkpoint, records);
            }
        };
    }


}