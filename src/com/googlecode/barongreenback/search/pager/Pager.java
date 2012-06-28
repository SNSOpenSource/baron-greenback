package com.googlecode.barongreenback.search.pager;

import com.googlecode.totallylazy.Sequence;

import java.util.List;
import java.util.Map;

public interface Pager {
    String CURRENT_PAGE_PARAM = "page.current";
    String ROWS_PER_PAGE_PARAM = "page.rows";

    <T> Sequence<T> paginate(Sequence<T> sequence);

    String getRowsPerPage();

    int getTotalRows();

    int getCurrentPage();

    int getNumberOfPages();

    String getQueryStringForPage(int pageNumber);

    boolean isPaged();

    List<Map.Entry<String, String>> getQueryParametersToUrl();
}
