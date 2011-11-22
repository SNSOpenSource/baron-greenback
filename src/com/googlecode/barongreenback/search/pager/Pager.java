package com.googlecode.barongreenback.search.pager;

import com.googlecode.totallylazy.Sequence;

public interface Pager {
    String CURRENT_PAGE_PARAM = "page.current";
    String ROWS_PER_PAGE_PARAM = "page.rows";

    <T> Sequence<T> paginate(Sequence<T> sequence);

    int getRowsPerPage();

    Number getTotalRows();

    int getCurrentPage();

    Number getNumberOfPages();

    String getQueryStringForPage(int pageNumber);

    boolean isPaged();
}
