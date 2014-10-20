package com.googlecode.barongreenback.less;

import com.googlecode.utterlyidle.Request;

import static com.googlecode.totallylazy.Option.option;

public class CacheUnlessPurge implements LessCssConfig {
    private Request request;

    public CacheUnlessPurge(Request request) {
        this.request = request;
    }

    @Override
    public boolean useCache() {
        return !option(request.uri().query()).getOrElse("").contains("purge");
    }
}
