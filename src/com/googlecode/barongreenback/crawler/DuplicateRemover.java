package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.lazyrecords.SelectCallable.select;

public class DuplicateRemover implements Feeder<Uri> {
    private final Feeder<Uri> feeder;

    public DuplicateRemover(Feeder<Uri> feeder) {
        this.feeder = feeder;
    }

    public Sequence<Record> get(Uri source, RecordDefinition definition) throws Exception {
        return feeder.get(source, definition).unique(select(uniqueFields(definition).map(ignoreAlias())));
    }

    public static Callable1<Keyword<?>, Keyword<?>> ignoreAlias() {
        return new Callable1<Keyword<?>, Keyword<?>>() {
            public Keyword call(Keyword<?> keyword) throws Exception {
                return Keywords.keyword(keyword.name(), keyword.forClass()).metadata(keyword.metadata());
            }
        };
    }
}
