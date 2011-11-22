package com.googlecode.barongreenback.search.pager;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;

import java.util.List;
import java.util.Map;

import static com.googlecode.totallylazy.Maps.pairToEntry;
import static com.googlecode.totallylazy.Sequences.sequence;

public class RequestPager implements Pager {
    private static final String DEFAULT_ROWS_PER_PAGE = "20";
    private static final String DEFAULT_PAGE = "1";

    private int currentPage;
    private String numberOfRowsPerPage;
    private Number totalRows;
    private Request request;


    public RequestPager(Request request) {
        this.request = request;
        final QueryParameters queryParameters = QueryParameters.parse(request.uri().query());

        currentPage = Integer.parseInt(Option.option(queryParameters.getValue(CURRENT_PAGE_PARAM)).getOrElse(DEFAULT_PAGE));
        numberOfRowsPerPage = Option.option(queryParameters.getValue(ROWS_PER_PAGE_PARAM)).getOrElse(DEFAULT_ROWS_PER_PAGE);
    }

    public <T> Sequence<T> paginate(Sequence<T> sequence) {
        totalRows = sequence.size();
        if (isShowingAllPages()) {
            currentPage = 1;
            return sequence;
        }

        int rowsPerPage = getRowsPerPageAsInteger();

        return sequence.drop(rowsPerPage * (currentPage - 1)).take(rowsPerPage);
    }

    private boolean isShowingAllPages() {
        return "ALL".equalsIgnoreCase(numberOfRowsPerPage);
    }

    private int getRowsPerPageAsInteger() {
        return Integer.parseInt(numberOfRowsPerPage);
    }

    public String getRowsPerPage() {
        return String.valueOf(numberOfRowsPerPage);
    }

    public Number getTotalRows() {
        return totalRows;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public Number getNumberOfPages() {
        if (isShowingAllPages()) {
            return 1;
        }
        return Math.ceil(totalRows.doubleValue() / getRowsPerPageAsInteger());
    }

    public String getQueryStringForPage(int pageNumber) {
        return QueryParameters.parse(request.uri().query()).remove(CURRENT_PAGE_PARAM).add(CURRENT_PAGE_PARAM, String.valueOf(pageNumber)).toString();
    }

    public boolean isPaged() {
        return getNumberOfPages().intValue() > 1;
    }

    public List<Map.Entry<String, String>> getQueryParametersToUrl() {
        return sequence(QueryParameters.parse(request.uri().query()).remove(ROWS_PER_PAGE_PARAM)).map(pairToEntry(String.class, String.class)).toList();
    }
}
