package com.googlecode.barongreenback.shared.sorter;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Requests;

import java.util.List;
import java.util.Map;

import static com.googlecode.funclate.Model.functions.asMap;
import static com.googlecode.funclate.Model.mutable;
import static com.googlecode.lazyrecords.Keyword.functions.name;
import static com.googlecode.lazyrecords.Keyword.methods.keywords;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class Sorter {

    public static final String SORT_COLUMN_QUERY_PARAM = "page.sort.column";
    public static final String SORT_DIRECTION_QUERY_PARAM = "page.sort.direction";
    public static final String ASCENDING_SORT_DIRECTION = "asc";
    public static final String DESCENDING_SORT_DIRECTION = "desc";

    private Option<String> sortColumn;
    private Option<String> sortDirection;
    private QueryParameters queryParameters;

    public Sorter(Request request) {
        queryParameters = Requests.query(request);
        this.sortColumn = queryParameters.valueOption(SORT_COLUMN_QUERY_PARAM);
        this.sortDirection = queryParameters.valueOption(SORT_DIRECTION_QUERY_PARAM);
    }

    private static List<Map<String, Object>> headers(Sequence<Keyword<?>> headers, Sequence<Record> results) {
        if (headers.isEmpty()) {
            return toModel(keywords(results).realise());
        }
        return toModel(headers);
    }

    private static List<Map<String, Object>> toModel(Sequence<Keyword<?>> keywords) {
        return keywords.map(asHeader()).
                map(asMap()).
                toList();
    }

    private static Callable1<? super Keyword, Model> asHeader() {
        return new Callable1<Keyword, Model>() {
            public Model call(Keyword keyword) throws Exception {
                return mutable.model().add("name", keyword.name());
            }
        };
    }

    public Sequence<Record> sort(Sequence<Record> results, Callable1<Option<String>, Keyword> keywordMapper) {
        final Keyword keyword = Callers.call(keywordMapper, sortColumn);
        if (DESCENDING_SORT_DIRECTION.equalsIgnoreCase(sortDirection.getOrElse(DESCENDING_SORT_DIRECTION))) {
            return results.sortBy(descending(keyword));
        }
        return results.sortBy(keyword);
    }

    public Sequence<Record> sort(Sequence<Record> results, Sequence<Keyword<?>> allHeaders) {
        Keyword keyword = allHeaders.find(where(name(), is(sortColumn.getOrElse(allHeaders.first().name())))).get();
        if (DESCENDING_SORT_DIRECTION.equalsIgnoreCase(sortDirection.getOrElse(DESCENDING_SORT_DIRECTION))) {
            return results.sortBy(descending(keyword));
        }
        return results.sortBy(keyword);
    }

    public String linkFor(Keyword keyword, Sequence<Keyword<?>> visibleHeaders) {
        QueryParameters parameters = queryParameters.remove(SORT_COLUMN_QUERY_PARAM).remove(SORT_DIRECTION_QUERY_PARAM).add(SORT_COLUMN_QUERY_PARAM, keyword.name());
        if (keyword.name().equals(sortColumn.getOrElse(visibleHeaders.head().name())) && !ASCENDING_SORT_DIRECTION.equals(sortDirection.getOrNull())) {
            parameters = parameters.add(SORT_DIRECTION_QUERY_PARAM, ASCENDING_SORT_DIRECTION);
        }

        return parameters.toString();
    }

    public String getSortedColumn(Sequence<Keyword<?>> visibleHeaders) {
        return sortColumn.getOrElse(visibleHeaders.head().name());
    }

    public boolean isSortedAscending() {
        return ASCENDING_SORT_DIRECTION.equalsIgnoreCase(sortDirection.getOrNull());
    }

    public Model model(Model model, Sequence<Keyword<?>> headers, Sequence<Record> paged) {
        return model.
                add("sorter", this).
                add("headers", headers(headers, paged)).
                add("sortLinks", sortLinks(headers)).
                add("sortedHeaders", sortedHeaders(headers));
    }

    private Map<String, String> sortedHeaders(Sequence<Keyword<?>> visibleHeaders) {
        return Maps.map(Pair.pair(getSortedColumn(visibleHeaders), isSortedAscending() ? "headerSortDown" : "headerSortUp"));
    }

    private Map<String, String> sortLinks(final Sequence<Keyword<?>> visibleHeaders) {
        Map<String, String> linkMap = Maps.map();
        return visibleHeaders.fold(linkMap, new Callable2<Map<String, String>, Keyword, Map<String, String>>() {
            public Map<String, String> call(Map<String, String> linkMap, Keyword keyword) throws Exception {
                linkMap.put(keyword.name(), linkFor(keyword, visibleHeaders));
                return linkMap;
            }
        });
    }
}
