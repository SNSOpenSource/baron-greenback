package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.*;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Xml.selectContents;

public class MoreDataCrawler {
    public Function1<Document, Document> getMoreIfNeeded(final Job job, final QueuesCrawler crawler) {
        return new Function1<Document, Document>() {
            @Override
            public Document call(Document document) throws Exception {
                for (Job moreWork : getMoreIfNeeded(job, document)) {
                    crawler.crawl(moreWork);
                }

                return document;
            }
        };
    }

    public Option<Job> getMoreIfNeeded(Job job, Document document) {
        DataSource original = job.dataSource();
        Uri moreUri = Uri.uri(selectContents(document, original.moreXPath()));

        if (!original.containsCheckpoint(document)) {
            DataSource dataSource = original.request(QueuesCrawler.requestFor(moreUri));
            return Option.some(Job.job(dataSource, job.destination()));
        }
        return Option.none();
    }
}