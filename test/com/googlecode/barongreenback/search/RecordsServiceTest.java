package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.RecordsModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.ImmutableKeyword;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Before;
import org.junit.Test;

import static com.googlecode.barongreenback.search.RecordsService.headers;
import static com.googlecode.barongreenback.search.RecordsService.unalias;
import static com.googlecode.barongreenback.views.ViewsRepository.GROUP;
import static com.googlecode.barongreenback.views.ViewsRepository.VISIBLE;
import static com.googlecode.barongreenback.views.ViewsRepository.viewModel;
import static com.googlecode.barongreenback.views.ViewsRepository.viewName;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Predicates.all;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class RecordsServiceTest {
    private BaronGreenbackRecords records;
    private Model view;
    private Record expected;
    private ImmutableKeyword<String> keyword;

    @Before
    public void initialiseRecords() {
        records = BaronGreenbackRecords.records(new MemoryRecords());
        keyword = keyword("foo", String.class);
        view = viewModel(Sequences.<Keyword<?>>sequence(keyword), "name", "records", "", true, "");
        Definition viewDefinition = definition(viewName(view), headers(view));
        expected = record().set(keyword, "bar");
        records.value().add(viewDefinition, expected);
    }

    @Test
    public void supportsDirectRecordAccess() throws Exception {
        final RecordsService recordsService = new RecordsService(records, new RecordsModelRepository(records), null);

        final Sequence<Record> results = recordsService.getRecords(view, all(Record.class));
        assertThat(results, contains(expected));
    }

    @Test
    public void shouldUnaliasKeywordsCorrectly() throws Exception {
        final AliasedKeyword<String> aliased = keyword.as("aliased");

        final Keyword<?> unaliased = unalias().call(aliased);

        assertThat(unaliased, instanceOf(ImmutableKeyword.class));
        assertThat(unaliased.name(), is("foo"));
    }

    @Test
    public void shouldPreserveMetadataWhenUnaliasing() throws Exception {
        final AliasedKeyword<String> aliasedKeyword = keyword.as("aliased").metadata(record(VISIBLE, true, GROUP, "Group"));

        final Keyword<?> unaliased = unalias().call(aliasedKeyword);

        assertThat(unaliased.metadata(VISIBLE).value(), is(true));
        assertThat(unaliased.metadata(GROUP).value(), is("Group"));
    }

}
