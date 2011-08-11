package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;

import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.dsl.BindingBuilder.definedParam;
import static com.googlecode.utterlyidle.proxy.Resource.resource;
import static com.googlecode.utterlyidle.proxy.Resource.urlOf;

public class ViewRenderer implements Callable1<View,String> {
    public String call(View view) throws Exception {
        return String.format("<a href=\"/%s\">%s</a>", urlOf(resource(SearchResource.class).find("+type:" + view.name())), view.name());
    }
}
