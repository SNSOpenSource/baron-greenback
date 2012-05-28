package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.Xml;
import org.w3c.dom.Document;

import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Xml.selectContents;

public class MoreDataCrawler {
    private final StringMappings mappings;
    private final QueuesCrawler crawler;

    public MoreDataCrawler(StringMappings mappings, QueuesCrawler crawler) {
        this.mappings = mappings;
        this.crawler = crawler;
    }

    public Function1<Document, Document> getMoreIfNeeded(final Definition source, final Definition destination, final Object checkpoint, final String moreXpath) {
        return new Function1<Document, Document>() {
            @Override
            public Document call(Document document) throws Exception {
                getMoreIfNeeded(document, moreXpath, source, destination, checkpoint);
                return document;
            }
        };
    }

    public void getMoreIfNeeded(Document document, String moreXpath, Definition source, Definition destination, Object checkpoint) {
        try {
            String xpath = checkpointXPathFrom(source);

            Sequence<String> checkPoints = Xml.selectNodes(document, xpath).map(Xml.contents());
            String rawCheckpoint = mappings.toString(checkpoint.getClass(), checkpoint);
            if(checkPoints.contains(rawCheckpoint)){
                return;
            }

            Uri uri = Uri.uri(selectContents(document, moreXpath));
            crawler.crawl(QueuesCrawler.requestFor(uri), source, destination, checkpoint, moreXpath);
        } catch (Exception ignored) {
        }
    }

    private String checkpointXPathFrom(Definition source) {
        Keyword<?> keyword = source.fields().find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).get();
        return String.format("%s/%s", source.name(), keyword.name());
    }
}
