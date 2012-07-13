package com.googlecode.barongreenback.shared;

import com.googlecode.totallylazy.proxy.Invocation;
import com.googlecode.utterlyidle.Response;

public interface InvocationHandler {
    public Response handle(final Invocation invocation) throws Exception;
}
