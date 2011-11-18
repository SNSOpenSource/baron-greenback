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

public class PagerTest {

    @Test
    public void defaultsTheCurrentPageAndRows() throws Exception {
        Request request = RequestBuilder.get("/somePath").build();
        Pager pager = new Pager(request);

        assertThat(pager.getCurrentPage(), is(1));
        assertThat(pager.getRowsPerPage(), is(20));
    }

    @Test
    public void getsCurrentPageAndRowsPerPageFromRequest() throws Exception {
        Request request = requestForCurrentPageAndRows(30, 25).build();
        Pager pager = new Pager(request);
        
        assertThat(pager.getCurrentPage(), is(30));
        assertThat(pager.getRowsPerPage(), is(25));
    }

    @Test
    public void paginateShouldReturnSequenceForNthPage() throws Exception {
        Pager pager = new Pager(requestForCurrentPageAndRows(4, 20).build());
        Sequence<Number> sequence = range(1, 101);

        Sequence<Number> paginatedSequence = pager.paginate(sequence);
        assertThat(paginatedSequence, is(range(61, 81)));
        assertThat(pager.getTotalRows(), NumberMatcher.is(100));
    }

    @Test
    public void getQueryForPageNIgnoredOtherParametersAndSetsPage() throws Exception {
        Pager pager = new Pager(requestForCurrentPageAndRows(5, 10).withQuery("dont", "touchme").build());

        QueryParameters parameters = QueryParameters.parse(removeLeadingQuestionMark(pager));
        assertThat(parameters.getValue(Pager.CURRENT_PAGE_PARAM), is("2"));
        assertThat(parameters.getValue(Pager.ROWS_PER_PAGE_PARAM), is("10"));
        
        Parameters actualOtherParams = parameters.remove(Pager.CURRENT_PAGE_PARAM);
        Parameters expectedOtherParams = QueryParameters.parse(requestForCurrentPageAndRows(5, 10).withQuery("dont", "touchme").build().uri().query()).remove(Pager.CURRENT_PAGE_PARAM);
        assertThat(actualOtherParams, is(expectedOtherParams));

    }

    private String removeLeadingQuestionMark(Pager pager) {
        return pager.getQueryStringForPage(2).substring(1);
    }

    private RequestBuilder requestForCurrentPageAndRows(final int currentPage, final int rowsPerPage) {
        return RequestBuilder.get("/somePath").withQuery(Pager.CURRENT_PAGE_PARAM, currentPage).withQuery(Pager.ROWS_PER_PAGE_PARAM, rowsPerPage);
    }
}
