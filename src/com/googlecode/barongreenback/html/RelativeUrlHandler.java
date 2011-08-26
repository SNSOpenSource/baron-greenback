package com.googlecode.barongreenback.html;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.io.HierarchicalPath;
import com.googlecode.utterlyidle.io.Url;

import static com.googlecode.utterlyidle.io.HierarchicalPath.hierarchicalPath;

public class RelativeUrlHandler implements HttpHandler {
    private final HttpHandler httpHandler;
    private Request lastRequest;

    public RelativeUrlHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public Response handle(Request request) throws Exception {
        if (lastRequest != null) {
            Url newUrl = request.url();
            String currentUrl = newUrl.toString();
            Url lastUrl = lastRequest.url();
            if (currentUrl.equals("")) {
                request.url(lastUrl);
            } else if (!currentUrl.startsWith("/")) {
                String newRelativePath = newUrl.path().toString();
                String oldPath = lastUrl.path().toString();
                String newAbsolutePath = oldPath.replaceFirst("/([^/]*)$", "/" + newRelativePath);
                request.url(newUrl.replacePath(hierarchicalPath(newAbsolutePath)));
            }
        }
        lastRequest = request;
        return httpHandler.handle(request);
    }
}