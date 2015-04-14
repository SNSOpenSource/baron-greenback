package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.RecordsModelRepository;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.lazyrecords.parser.StandardParser;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Before;
import org.junit.Test;

import static com.googlecode.barongreenback.search.RecordsService.*;
import static com.googlecode.barongreenback.views.ViewsRepository.*;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Predicates.all;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

    @Test
    public void prefxingQueryShouldSupportORs() throws Exception {
        final String prefixedQuery = prefixQueryWithImplicitViewQuery(viewWithQuery("keywordA:foo"), "keywordB:stu OR keywordA:foobar");

        final Record shouldMatch = record(keyword("keywordA"), "foo", keyword("keywordB"), "stu");
        final Record shouldNotMatch = record(keyword("keywordA"), "foobar", keyword("keywordB"), "stu");

        assertThat(parsedQuery(prefixedQuery).matches(shouldMatch), is(true));
        assertThat(parsedQuery(prefixedQuery).matches(shouldNotMatch), is(false));
    }

    @Test
    public void prefixingShouldSupportEmptyQueries() throws Exception {
        final String prefixedQuery = prefixQueryWithImplicitViewQuery(viewWithQuery("keywordA:foo"), "");

        final Record shouldMatch = record(keyword("keywordA"), "foo");
        final Record shouldNotMatch = record(keyword("keywordA"), "foobar");

        assertThat(parsedQuery(prefixedQuery).matches(shouldMatch), is(true));
        assertThat(parsedQuery(prefixedQuery).matches(shouldNotMatch), is(false));
    }

    @Test
    public void prefixingShouldSupportEmptyViews() throws Exception {
        final String prefixedQuery = prefixQueryWithImplicitViewQuery(viewWithQuery(""), "keywordA:foo");

        final Record shouldMatch = record(keyword("keywordA"), "foo");
        final Record shouldNotMatch = record(keyword("keywordA"), "foobar");

        assertThat(parsedQuery(prefixedQuery).matches(shouldMatch), is(true));
        assertThat(parsedQuery(prefixedQuery).matches(shouldNotMatch), is(false));
    }

    @Test
    public void prefixingShouldSupportEmptyArguments() throws Exception {
        final String prefixedQuery = prefixQueryWithImplicitViewQuery(viewWithQuery(""), "");

        final Record shouldMatch = record(keyword("keywordA"), "foo");

        assertThat(parsedQuery(prefixedQuery).matches(shouldMatch), is(true));
    }

    @Test
    public void prefixingShouldHandleWhitespace() throws Exception {
        final String prefixedQuery = prefixQueryWithImplicitViewQuery(viewWithQuery("keywordA:foo "), "");

        final Record shouldMatch = record(keyword("keywordA"), "foo");

        assertThat(parsedQuery(prefixedQuery).matches(shouldMatch), is(true));
    }

    private Model viewWithQuery(String query) {
        return ViewsRepository.viewModel(Sequences.<Keyword<?>>empty(), "viewName", "source", query, true, "");
    }

    private Predicate<Record> parsedQuery(String prefixedQuery) {
        return new StandardParser().parse(prefixedQuery, Sequences.<Keyword<?>>empty());
    }

}
