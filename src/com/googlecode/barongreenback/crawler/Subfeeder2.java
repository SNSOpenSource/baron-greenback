package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Predicates.*;

public class Subfeeder2 {
    public static Function1<Sequence<Record>, Sequence<Job>> subfeeds(final Definition destination) {
        return new Function1<Sequence<Record>, Sequence<Job>>() {
            @Override
            public Sequence<Job> call(Sequence<Record> records) throws Exception {
                return subfeeds(records, destination);
            }
        };
    }

    public static Sequence<Job> subfeeds(Sequence<Record> records, Definition destination) {
        return records.flatMap(subfeedsKeywords(destination));
    }

    private static Callable1<Record, Sequence<Job>> subfeedsKeywords(final Definition destination) {
        return new Callable1<Record, Sequence<Job>>() {
            public Sequence<Job> call(final Record record) throws Exception {
                Sequence<Keyword<?>> subfeedKeywords = record.keywords().filter(where(metadata(RECORD_DEFINITION), is(Predicates.notNullValue()))).realise();
                return subfeedKeywords.map(toJob(record, destination));
            }
        };
    }

    private static Callable1<Keyword<?>, Job> toJob(final Record record, final Definition destination) {
        return new Callable1<Keyword<?>, Job>() {
            @Override
            public Job call(Keyword<?> keyword) throws Exception {
                Object value = record.get(keyword);
                Uri uri = Uri.uri(value.toString());
                return Job.job(HttpDataSource.dataSource(uri, keyword.metadata().get(RECORD_DEFINITION).definition()), destination);
            }
        };
    }
}