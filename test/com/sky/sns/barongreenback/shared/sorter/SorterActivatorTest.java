package com.sky.sns.barongreenback.shared.sorter;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Triple;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import com.sky.sns.barongreenback.crawler.failures.FailureResource;
import com.sky.sns.barongreenback.search.SorterActivator;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Triple.triple;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.ID;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.REQUEST_TIME;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.URI;
import static com.sky.sns.barongreenback.shared.sorter.Sorter.SORT_COLUMN_QUERY_PARAM;
import static com.sky.sns.barongreenback.shared.sorter.Sorter.SORT_DIRECTION_QUERY_PARAM;
import static com.sky.sns.barongreenback.shared.sorter.Sorter.sortKeywordFromRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SorterActivatorTest {

    @Test
    public void readsKeywordToSortFromRequest() throws Exception {
        Sorter firstSorter = new SorterActivator(RequestBuilder.get("/somePath").query(SORT_COLUMN_QUERY_PARAM, "mooCow").query(SORT_DIRECTION_QUERY_PARAM, "asc").build()).call();
        assertThat(firstSorter.sort(records(), sortKeywordFromRequest(keywords())).map(asSecondKeyword()).safeCast(String.class), is(sequence("1", "2", "3")));

        Sorter secondSorter = new SorterActivator(RequestBuilder.get("/somePath").query(SORT_COLUMN_QUERY_PARAM, "blueCow").build()).call();
        assertThat(secondSorter.sort(records(), sortKeywordFromRequest(keywords())).map(asThirdKeyword()).safeCast(Date.class), is(sequence(new Date(100000), new Date(10000), new Date(1))));
    }

    @Test
    public void defaultsToFirstColumnWhenKeywordMissing() throws Exception {
        Sorter sorterWithNoParam = new SorterActivator(RequestBuilder.get("/somePath").build()).call();
        assertThat(sorterWithNoParam.sort(records(), sortKeywordFromRequest(keywords())).map(asFirstKeyword()).safeCast(String.class), is(sequence("C", "B", "A")));
    }

    @Test
    public void usesDescendingOrderWhenSpecified() throws Exception {
        Sorter sorter =  new SorterActivator(RequestBuilder.get("/somePath").query(SORT_COLUMN_QUERY_PARAM, "mooCow").query(SORT_DIRECTION_QUERY_PARAM, "desc").build()).call();
        assertThat(sorter.sort(records(), sortKeywordFromRequest(keywords())).map(asSecondKeyword()).safeCast(String.class), is(sequence("3", "2", "1")));
    }

    @Test
    public void defaultsToRequestTimeIfOnTheCrawlerFailuresPage() throws Exception {
        final Request crawlerFailuresRequest = RequestBuilder.get(relativeUriOf(method(on(FailureResource.class).list(Option.<String>none(), "")))).build();
        Sorter sorter = new SorterActivator(crawlerFailuresRequest).call();
        assertThat(sorter.getSortedColumn(crawlerFailureKeywords()), is("request time"));
        assertThat(sorter.isSortedAscending(), is(false));
    }

    @Test
    public void usesTheSortParametersIfSpecified() throws Exception {
        final Request crawlerFailuresRequest = RequestBuilder.get(relativeUriOf(method(on(FailureResource.class).list(Option.<String>none(), ""))))
                .query(SORT_COLUMN_QUERY_PARAM, "uri")
                .query(SORT_DIRECTION_QUERY_PARAM, "asc").build();
        Sorter sorter = new SorterActivator(crawlerFailuresRequest).call();
        assertThat(sorter.getSortedColumn(crawlerFailureKeywords()), is("uri"));
        assertThat(sorter.isSortedAscending(), is(true));
    }


    private Callable1<? super Record, Object> asFirstKeyword() {
        return new Callable1<Record, Object>() {
            public Object call(Record record) throws Exception {
                return record.get(keywords().first());
            }
        };
    }

    private Callable1<? super Record, Object> asThirdKeyword() {
        return new Callable1<Record, Object>() {
            public Object call(Record record) throws Exception {
                return record.get(keywords().last());
            }
        };
    }

    private Callable1<Record, Object> asSecondKeyword() {
        return new Callable1<Record, Object>() {
            public Object call(Record record) throws Exception {
                return record.get(keywords().second());
            }
        };
    }

    private Sequence<Keyword<?>> keywords() {
        return Sequences.<Keyword<?>>sequence(keyword("stuCow"), keyword("mooCow"), keyword("blueCow", Date.class));
    }

    private Sequence<Record> records() {
        Sequence<Triple<String, String, Date>> baseRecords = sequence(triple("A", "2", new Date(100000)), triple("B", "3", new Date(10000)), triple("C", "1", new Date(1)));

        return baseRecords.map(new Callable1<Triple<String, String, Date>, Record>() {
            public Record call(Triple<String, String, Date> triple) throws Exception {
                return Record.constructors.record().
                        set(Unchecked.<Keyword<Object>>cast(keywords().first()), triple.first()).
                        set(Unchecked.<Keyword<Object>>cast(keywords().second()), triple.second()).
                        set(Unchecked.<Keyword<Object>>cast(keywords().last()), triple.third());
            }
        });
    }

    private Sequence<Keyword<?>> crawlerFailureKeywords() {
        return Sequences.<Keyword<?>>sequence(ID, URI, REQUEST_TIME);
    }
}
