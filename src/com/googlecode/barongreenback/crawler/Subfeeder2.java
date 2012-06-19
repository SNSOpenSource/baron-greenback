package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keywords.UNIQUE;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Predicates.*;

public class Subfeeder2 {
    public static Sequence<HttpJob> subfeeds(Sequence<Record> records, Definition destination, Sequence<Record> merged) {
        return records.flatMap(subfeedsKeywords(destination, merged));
    }

    private static Callable1<Record, Sequence<HttpJob>> subfeedsKeywords(final Definition destination, final Sequence<Record> merged) {
        return new Callable1<Record, Sequence<HttpJob>>() {
            public Sequence<HttpJob> call(final Record record) throws Exception {
                Sequence<Keyword<?>> subfeedKeywords = record.keywords().filter(where(metadata(RECORD_DEFINITION), is(Predicates.notNullValue()))).realise();
                return subfeedKeywords.map(toJob(record, destination, merged));
            }
        };
    }

    private static Callable1<Keyword<?>, HttpJob> toJob(final Record record, final Definition destination, final Sequence<Record> merged) {
        return new Callable1<Keyword<?>, HttpJob>() {
            @Override
            public HttpJob call(Keyword<?> keyword) throws Exception {
                return job(keyword, record, destination, merged);
            }
        };
    }

    public static HttpJob job(Keyword<?> subfeedKeyword, Record record, Definition destination, Sequence<Record> merged) {
        Object value = record.get(subfeedKeyword);
        Uri uri = Uri.uri(value.toString());

        Sequence<Pair<Keyword<?>, Object>> fields = merged.flatMap(new Callable1<Record, Sequence<Pair<Keyword<?>, Object>>>() {
            @Override
            public Sequence<Pair<Keyword<?>, Object>> call(Record record) throws Exception {
                return record.fields();
            }
        });

        Sequence<Pair<Keyword<?>, Object>> keysAndValues = fields.join(record.fields().filter(where(Callables.<Keyword<?>>first(), where(metadata(UNIQUE), is(true))))).realise();

        return HttpJob.job(SubfeedDatasource.dataSource(uri, subfeedKeyword.metadata().get(RECORD_DEFINITION).definition(), keysAndValues), destination);
    }

    public static Sequence<Record> mergePreviousUniqueIdentifiers(Sequence<Record> records, final HttpDataSource dataSource) {
        if(dataSource instanceof SubfeedDatasource){
            return records.map(new Callable1<Record, Record>() {
                @Override
                public Record call(Record record) throws Exception {
                    return record(record.fields().join(((SubfeedDatasource) dataSource).uniqueIdentifiers()));
                }
            });
        }
        return records;
   }
}