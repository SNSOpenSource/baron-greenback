package com.googlecode.barongreenback.views;

import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;

import java.util.concurrent.Callable;

public class CurrentViewFromRequestActivator implements Callable<CurrentView> {
    private final Request request;

    public CurrentViewFromRequestActivator(Request request) {
        this.request = request;
    }

    @Override
    public CurrentView call() throws Exception {
        final QueryParameters queryParameters = QueryParameters.parse(request.uri().query());
        final String viewName = queryParameters.getValue("current");
        return new CurrentView(viewName);
    }
}
