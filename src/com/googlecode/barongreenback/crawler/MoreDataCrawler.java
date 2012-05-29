package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.*;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Xml.selectContents;

public class MoreDataCrawler {
    public Function1<Document, Option<Job>> getMoreIfNeeded(final Job job) {
        return new Function1<Document, Option<Job>>() {
            @Override
            public Option<Job> call(Document document) throws Exception {
                return getMoreIfNeeded(job, document);
            }
        };
    }

    public Option<Job> getMoreIfNeeded(Job job, Document document) {
        DataSource original = job.dataSource();
        Uri moreUri = Uri.uri(selectContents(document, original.moreXPath()));

        if (!original.containsCheckpoint(document)) {
            DataSource dataSource = original.request(moreUri);
            return Option.some(Job.job(dataSource, job.destination()));
        }
        return Option.none();
    }
}