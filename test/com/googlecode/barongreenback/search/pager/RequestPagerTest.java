package com.googlecode.barongreenback.search.pager;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import com.googlecode.utterlyidle.Parameters;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import org.junit.Test;

import static com.googlecode.totallylazy.numbers.Numbers.range;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RequestPagerTest {

    @Test
    public void defaultsTheCurrentPageAndRows() throws Exception {
        Request request = RequestBuilder.get("/somePath").build();
        Pager pager = new RequestPager(request);

        assertThat(pager.getCurrentPage(), is(1));
        assertThat(pager.getRowsPerPage(), is("20"));
    }

    @Test
    public void getsCurrentPageAndRowsPerPageFromRequest() throws Exception {
        Request request = requestForCurrentPageAndRows(30, 25).build();
        Pager pager = new RequestPager(request);
        
        assertThat(pager.getCurrentPage(), is(30));
        assertThat(pager.getRowsPerPage(), is("25"));
    }

    @Test
    public void paginateShouldReturnSequenceForNthPage() throws Exception {
        Pager pager = new RequestPager(requestForCurrentPageAndRows(4, 20).build());
        Sequence<Number> sequence = range(1, 100);

        Sequence<Number> paginatedSequence = pager.paginate(sequence);
        assertThat(paginatedSequence, is(range(61, 80)));
        assertThat(pager.getTotalRows(), NumberMatcher.is(100));
    }

    @Test
    public void getQueryForPageNIgnoredOtherParametersAndSetsPage() throws Exception {
        Pager pager = new RequestPager(requestForCurrentPageAndRows(5, 10).query("dont", "touchme").build());

        QueryParameters parameters = QueryParameters.parse(removeLeadingQuestionMark(pager));
        assertThat(parameters.getValue(Pager.CURRENT_PAGE_PARAM), is("2"));
        assertThat(parameters.getValue(Pager.ROWS_PER_PAGE_PARAM), is("10"));
        
        Parameters actualOtherParams = parameters.remove(Pager.CURRENT_PAGE_PARAM);
        Parameters expectedOtherParams = QueryParameters.parse(requestForCurrentPageAndRows(5, 10).query("dont", "touchme").build().uri().query()).remove(Pager.CURRENT_PAGE_PARAM);
        assertThat(actualOtherParams, is(expectedOtherParams));
    }

    @Test
    public void supportsAllParameter() throws Exception {
        Pager pager = new RequestPager(RequestBuilder.get("/somePage").query(Pager.ROWS_PER_PAGE_PARAM, "ALL").build());
        Sequence<Number> sequence = range(1, 100000).realise();

        Sequence<Number> paginatedSequence = pager.paginate(sequence);
        assertThat(paginatedSequence, is(sequence));
        assertThat(pager.getTotalRows(), is(sequence.size()));
        assertThat(pager.getNumberOfPages(), NumberMatcher.is(1));
    }

    @Test
    public void ignoredPagesNumbersWhenShowingAllRecords() throws Exception {
        Pager pager = new RequestPager(RequestBuilder.get("/somePage").query(Pager.CURRENT_PAGE_PARAM, 25).query(Pager.ROWS_PER_PAGE_PARAM, "100001").build());
        Sequence<Number> sequence = range(1, 100000).realise();

        Sequence<Number> paginatedSequence = pager.paginate(sequence);
        assertThat(paginatedSequence, is(sequence));
        assertThat(pager.getTotalRows(), is(sequence.size()));
        assertThat(pager.getNumberOfPages(), NumberMatcher.is(1));
    }

    private String removeLeadingQuestionMark(Pager pager) {
        return pager.getQueryStringForPage(2).substring(1);
    }

    private RequestBuilder requestForCurrentPageAndRows(final int currentPage, final int rowsPerPage) {
        return RequestBuilder.get("/somePath").query(Pager.CURRENT_PAGE_PARAM, currentPage).query(Pager.ROWS_PER_PAGE_PARAM, rowsPerPage);
    }
}
