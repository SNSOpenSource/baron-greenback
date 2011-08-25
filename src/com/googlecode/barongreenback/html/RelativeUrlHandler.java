package com.googlecode.barongreenback.html;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.MemoryRequest;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Requests;
import com.googlecode.utterlyidle.Response;

public class RelativeUrlHandler implements HttpHandler {
    private final HttpHandler httpHandler;
    private Request lastRequest;

    public RelativeUrlHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public Response handle(Request request) throws Exception {
        if(lastRequest != null && request.url().toString().equals("")){
            request.url(lastRequest.url());
        }
        lastRequest = request;
        return httpHandler.handle(request);
    }
}