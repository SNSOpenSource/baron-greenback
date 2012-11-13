package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.RecordsModelRepository;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Before;
import org.junit.Test;

import static com.googlecode.barongreenback.search.RecordsService.headers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RecordsServiceTest {
    private BaronGreenbackRecords records;
    private Model view;
    private Record expected;

    @Before
    public void initialiseRecords() {
        this.records = BaronGreenbackRecords.records(new MemoryRecords());
        Keyword<String> foo = Keywords.keyword("foo", String.class);
        this.view = ViewsRepository.viewModel(Sequences.<Keyword<?>>sequence(foo), "name", "records", "", true, "");
        Definition viewDefinition = Definition.constructors.definition(ViewsRepository.viewName(view), headers(view));
        expected = Record.constructors.record().set(foo, "bar");
        records.value().add(viewDefinition, expected);
    }

    @Test
    public void supportsDirectRecordAccess() throws Exception {
        RecordsService recordsService = new RecordsService(records, new RecordsModelRepository(records), null);

        Sequence<Record> results = recordsService.getRecords(view, Predicates.all(Record.class));
        assertThat(results, is(Sequences.one(expected)));
    }

}
