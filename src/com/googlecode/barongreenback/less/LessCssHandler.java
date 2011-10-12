package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.HttpHeaders;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.rendering.ExceptionRenderer;

import java.io.IOException;

import static com.googlecode.utterlyidle.RequestBuilder.get;

public class LessCssHandler implements HttpHandler {
    private final HttpHandler httpHandler;
    private final LessCompiler lessCompiler;

    public LessCssHandler(HttpHandler httpHandler, LessCompiler lessCompiler) {
        this.httpHandler = httpHandler;
        this.lessCompiler = lessCompiler;
    }

    public Response handle(Request request) throws Exception {
        Response response = httpHandler.handle(request);

        Uri uri = request.uri();
        if (!uri.path().endsWith(".less")) {
            return response;
        }
        String less = new String(response.bytes());
        return response.bytes(processLess(uri, less).getBytes("UTF-8"));
    }

    private String processLess(Uri uri, String less) throws IOException {
        return lessCompiler.compile(less, new Foo(uri));
    }

    public class Foo implements Callable1<String, String> {
        private Uri uri;

        public Foo(Uri uri) {
            this.uri = uri;
        }

        public String call(String newUri) throws Exception {
            try {
                uri = uri.mergePath(newUri);
                Response response = httpHandler.handle(get(uri).header(HttpHeaders.ACCEPT, MediaType.TEXT_CSS).build());
                return new String((byte[]) response.entity());
            } catch (Exception e) {
                return ExceptionRenderer.toString(e);
            }
        }
    }
}
