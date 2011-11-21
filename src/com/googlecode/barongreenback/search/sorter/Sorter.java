package com.googlecode.barongreenback.search.sorter;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.Parameters;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;

import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.records.Keywords.name;

public class Sorter {

    public static final String SORT_COLUMN_QUERY_PARAM = "page.sort.column";
    public static final String SORT_DIRECTION_QUERY_PARAM = "page.sort.direction";
    public static final String DESCENDING_SORT_DIRECTION = "desc";

    private String sortColumn;
    private String sortDirection;
    private QueryParameters queryParameters;

    public Sorter(Request request) {
        queryParameters = QueryParameters.parse(request.uri().query());
        this.sortColumn = queryParameters.getValue(SORT_COLUMN_QUERY_PARAM);
        this.sortDirection = queryParameters.getValue(SORT_DIRECTION_QUERY_PARAM);
    }

    public Sequence<Record> sort(Sequence<Record> results, Sequence<Keyword> allHeaders) {
        Keyword keyword = allHeaders.find(where(name(), is(option(sortColumn).getOrElse(allHeaders.first().name())))).get();
        if (DESCENDING_SORT_DIRECTION.equalsIgnoreCase(sortDirection)) {
            return results.sortBy(descending(keyword));
        }
        return results.sortBy(keyword);
    }

    public String linkFor(Keyword keyword, Sequence<Keyword> visibleHeaders) {
        Parameters parameters = queryParameters.remove(SORT_COLUMN_QUERY_PARAM).remove(SORT_DIRECTION_QUERY_PARAM).add(SORT_COLUMN_QUERY_PARAM, keyword.name());
        if (keyword.name().equals(option(sortColumn).getOrElse(visibleHeaders.head().name())) && !DESCENDING_SORT_DIRECTION.equals(sortDirection)) {
            parameters.add(SORT_DIRECTION_QUERY_PARAM, DESCENDING_SORT_DIRECTION);
        }

        return parameters.toString();
    }

    public String getSortedColumn(Sequence<Keyword> visibleHeaders) {
        return option(sortColumn).getOrElse(visibleHeaders.head().name());
    }

    public boolean isSortedDescending() {
        return DESCENDING_SORT_DIRECTION.equalsIgnoreCase(sortDirection);
    }
}
