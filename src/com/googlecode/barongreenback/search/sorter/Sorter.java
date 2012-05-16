package com.googlecode.barongreenback.search.sorter;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Parameters;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Requests;

import java.util.Map;

import static com.googlecode.lazyrecords.Keywords.name;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class Sorter {

    public static final String SORT_COLUMN_QUERY_PARAM = "page.sort.column";
    public static final String SORT_DIRECTION_QUERY_PARAM = "page.sort.direction";
    public static final String DESCENDING_SORT_DIRECTION = "desc";

    private String sortColumn;
    private String sortDirection;
    private QueryParameters queryParameters;

    public Sorter(Request request) {
        queryParameters = Requests.query(request);
        this.sortColumn = queryParameters.getValue(SORT_COLUMN_QUERY_PARAM);
        this.sortDirection = queryParameters.getValue(SORT_DIRECTION_QUERY_PARAM);
    }

    public Sequence<Record> sort(Sequence<Record> results, Sequence<Keyword<?>> allHeaders) {
        Keyword keyword = allHeaders.find(where(name(), is(option(sortColumn).getOrElse(allHeaders.first().name())))).get();
        if (DESCENDING_SORT_DIRECTION.equalsIgnoreCase(sortDirection)) {
            return results.sortBy(descending(keyword));
        }
        return results.sortBy(keyword);
    }

    public String linkFor(Keyword keyword, Sequence<Keyword<?>> visibleHeaders) {
        QueryParameters parameters = queryParameters.remove(SORT_COLUMN_QUERY_PARAM).remove(SORT_DIRECTION_QUERY_PARAM).add(SORT_COLUMN_QUERY_PARAM, keyword.name());
        if (keyword.name().equals(option(sortColumn).getOrElse(visibleHeaders.head().name())) && !DESCENDING_SORT_DIRECTION.equals(sortDirection)) {
            parameters = parameters.add(SORT_DIRECTION_QUERY_PARAM, DESCENDING_SORT_DIRECTION);
        }

        return parameters.toString();
    }

    public String getSortedColumn(Sequence<Keyword<?>> visibleHeaders) {
        return option(sortColumn).getOrElse(visibleHeaders.head().name());
    }

    public boolean isSortedDescending() {
        return DESCENDING_SORT_DIRECTION.equalsIgnoreCase(sortDirection);
    }

    public Map<String, String> sortedHeaders(Sequence<Keyword<?>> visibleHeaders) {
        return Maps.map(Pair.pair(getSortedColumn(visibleHeaders), isSortedDescending() ? "headerSortUp" : "headerSortDown"));
    }

    public Map<String, String> sortLinks(final Sequence<Keyword<?>> visibleHeaders) {
        Map<String, String> linkMap = Maps.map();
        return visibleHeaders.fold(linkMap, new Callable2<Map<String, String>, Keyword, Map<String, String>>() {
            public Map<String, String> call(Map<String, String> linkMap, Keyword keyword) throws Exception {
                linkMap.put(keyword.name(), linkFor(keyword, visibleHeaders));
                return linkMap;
            }
        });
    }

}
