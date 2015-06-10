package com.sky.sns.barongreenback.search;

import com.googlecode.totallylazy.Option;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Requests;
import com.sky.sns.barongreenback.crawler.failures.FailureRepository;
import com.sky.sns.barongreenback.shared.sorter.Sorter;

import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Option.some;
import static com.sky.sns.barongreenback.shared.sorter.Sorter.DESCENDING_SORT_DIRECTION;
import static com.sky.sns.barongreenback.shared.sorter.Sorter.SORT_COLUMN_QUERY_PARAM;
import static com.sky.sns.barongreenback.shared.sorter.Sorter.SORT_DIRECTION_QUERY_PARAM;

public class SorterActivator implements Callable<Sorter> {
    private final Request request;

    public SorterActivator(Request request) {
        this.request = request;
    }

    @Override
    public Sorter call() throws Exception {
        final QueryParameters queryParameters = Requests.query(request);
        final Option<String> sortColumn = queryParameters.valueOption(SORT_COLUMN_QUERY_PARAM);
        final Option<String> sortDirection = queryParameters.valueOption(SORT_DIRECTION_QUERY_PARAM);
        if (request.uri().dropQuery().toString().contains("crawler/failures/list") && sortColumn.isEmpty()) {
            return new Sorter(queryParameters, some(FailureRepository.REQUEST_TIME.name()), some(DESCENDING_SORT_DIRECTION));
        }
        return new Sorter(queryParameters, sortColumn, sortDirection);
    }
}
