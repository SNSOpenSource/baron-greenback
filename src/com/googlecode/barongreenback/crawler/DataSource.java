package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Xml;
import com.googlecode.utterlyidle.Request;
import org.w3c.dom.Document;

import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class DataSource {
    private final Request request;
    private final Definition source;
    private final Object checkpoint;
    private final String moreXPath;
    private final String checkpointAsString;
    private final String checkpointXPath;

    private DataSource(Request request, Definition source, Object checkpoint, String moreXPath, String checkpointAsString, String checkpointXPath) {
        this.request = request;
        this.source = source;
        this.checkpoint = checkpoint;
        this.moreXPath = moreXPath;
        this.checkpointAsString = checkpointAsString;
        this.checkpointXPath = checkpointXPath;
    }

    public static DataSource dataSource(Request request, Definition source, Object checkpoint, String moreXPath, StringMappings mappings) {
        return dataSource(request, source, checkpoint, moreXPath, checkpointAsString(mappings, checkpoint), checkpointXPath(source));
    }

    public static DataSource dataSource(Request request, Definition source, Object checkpoint, String moreXPath, String checkpointAsString, String checkpointXPath) {
        return new DataSource(request, source, checkpoint, moreXPath, checkpointAsString, checkpointXPath);
    }

    public Request request() {
        return request;
    }

    public Definition definition() {
        return source;
    }

    public Object checkpoint() {
        return checkpoint;
    }

    public String moreXPath() {
        return moreXPath;
    }

    public DataSource request(Request request) {
        return dataSource(request, source, checkpoint, moreXPath, checkpointAsString, checkpointXPath);
    }

    public static String checkpointXPath(Definition source) {
        Keyword<?> keyword = source.fields().find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).get();
        return String.format("%s/%s", source.name(), keyword.name());
    }

    public static String checkpointAsString(StringMappings mappings, Object checkpoint) {
        return mappings.toString(checkpoint.getClass(), checkpoint);
    }

    public boolean containsCheckpoint(Document document) {
        return selectCheckpoints(document).contains(checkpointAsString);
    }

    private Sequence<String> selectCheckpoints(Document document) {
        return Xml.selectNodes(document, checkpointXPath).map(Xml.contents());
    }
}