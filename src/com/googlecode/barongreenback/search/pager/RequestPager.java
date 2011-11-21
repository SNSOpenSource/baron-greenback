package com.googlecode.barongreenback.search.pager;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;

public class RequestPager implements Pager {
    private static final String DEFAULT_ROWS_PER_PAGE = "20";
    private static final String DEFAULT_PAGE = "1";

    private int currentPage;
    private int numberOfRowsPerPage;
    private Number totalRows;
    private QueryParameters queryParameters;

    public RequestPager(Request request) {
        queryParameters = QueryParameters.parse(request.uri().query());

        currentPage = Integer.parseInt(Option.option(queryParameters.getValue(CURRENT_PAGE_PARAM)).getOrElse(DEFAULT_PAGE));
        numberOfRowsPerPage = Integer.parseInt(Option.option(queryParameters.getValue(ROWS_PER_PAGE_PARAM)).getOrElse(DEFAULT_ROWS_PER_PAGE));
    }

    public <T> Sequence<T> paginate(Sequence<T> sequence) {
        totalRows = sequence.size();

        return sequence.drop(numberOfRowsPerPage * (currentPage - 1)).take(numberOfRowsPerPage);
    }

    public int getRowsPerPage() {
        return numberOfRowsPerPage;
    }

    public Number getTotalRows() {
        return totalRows;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public Number getNumberOfPages() {
        return Math.ceil(totalRows.doubleValue() / numberOfRowsPerPage);
    }

    public String getQueryStringForPage(int pageNumber) {
        return queryParameters.remove(CURRENT_PAGE_PARAM).add(CURRENT_PAGE_PARAM, String.valueOf(pageNumber)).toString();
    }
}
