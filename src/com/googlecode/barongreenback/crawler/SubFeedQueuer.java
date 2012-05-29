package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

import static com.googlecode.barongreenback.crawler.ConcurrentCrawler.SubFeedCrawler.uniqueKeysAndValues;
import static com.googlecode.barongreenback.crawler.QueuesCrawler.requestFor;
import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.predicates.WherePredicate.where;

public class SubFeedQueuer {
    private final QueuesCrawler crawler;

    public SubFeedQueuer(QueuesCrawler crawler) {
        this.crawler = crawler;
    }

    private Function1<Sequence<Record>, Sequence<Record>> queueSubFeeds(final Definition destination, final Sequence<Pair<Keyword<?>, Object>> keys) {
        return new Function1<Sequence<Record>, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Sequence<Record> records) throws Exception {
                return queueSubFeeds(records, destination, keys);
            }
        };
    }

    private Sequence<Record> queueSubFeeds(Sequence<Record> records, Definition destination, Sequence<Pair<Keyword<?>, Object>> keys) {
        for (Record record : records) {
            Sequence<Keyword<?>> subFeedKeys = record.keywords().
                    filter(where(metadata(RECORD_DEFINITION), is(notNullValue()))).
                    realise(); // Must Realise so we don't get concurrent modification as we add new fields to the record

            Sequence<Pair<Keyword<?>, Object>> allKeys = keys.join(uniqueKeysAndValues(record));
            System.out.println("allKeys = " + allKeys);

            for (Keyword<?> subFeedKey : subFeedKeys) {
                Object value = record.get(subFeedKey);
                if (value == null) {
                    continue;
                }

                Uri subFeed = uri(value.toString());
                Definition newSource = subFeedKey.metadata().get(RECORD_DEFINITION).definition();
//                crawler.crawl(Job.job(DataSource.dataSource(requestFor(subFeed), newSource, null, null), destination));
            }
        }
        return records;
    }

}
