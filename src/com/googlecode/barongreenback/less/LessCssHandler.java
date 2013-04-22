package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.HttpHeaders;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.ResponseBuilder;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.rendering.ExceptionRenderer;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.less.CachedLessCss.functions.less;
import static com.googlecode.barongreenback.less.CachedLessCss.functions.modifiedSince;
import static com.googlecode.utterlyidle.HttpHeaders.LAST_MODIFIED;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.Response.methods.header;

public class LessCssHandler implements HttpHandler {
    private final LessCssCache cache;
    private final HttpHandler httpHandler;
    private final LessCompiler lessCompiler;

    public LessCssHandler(HttpHandler httpHandler, LessCompiler lessCompiler, LessCssConfig config, LessCssCache cache) {
        this.httpHandler = httpHandler;
        this.lessCompiler = lessCompiler;
        this.cache = config.useCache() ? cache : new NoLessCssCache();
    }

    public Response handle(Request request) throws Exception {
        Response response = httpHandler.handle(request);

        Uri uri = request.uri();
        if (!(uri.path().endsWith(".less") && response.status().equals(Status.OK))) {
            return response;
        }
        String less = response.entity().toString();
        Date lastModified = Dates.RFC822().parse(header(response, LAST_MODIFIED));
        return ResponseBuilder.modify(response).entity(processLess(uri, less, lastModified)).build();
    }

    private String processLess(final Uri uri, final String rawLess, final Date lastModified) throws IOException {
        final String key = uri.path();

        return cache.getOption(key).
                filter(modifiedSince(lastModified)).
                map(less).
                getOrElse(compileAndCache(uri, rawLess, lastModified, key));
    }

    private Callable<String> compileAndCache(final Uri uri, final String rawLess, final Date lastModified, final String key) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                String result = lessCompiler.compile(rawLess, new Loader(uri));
                cache.put(key, new CachedLessCss(result, lastModified));
                return result;
            }
        };
    }

    public class Loader implements Callable1<String, String> {
        private Uri uri;

        public Loader(Uri uri) {
            this.uri = uri;
        }

        public Uri uri() {
            return uri;
        }

        public String call(String newUri) throws Exception {
            try {
                uri = uri.mergePath(newUri);
                Response response = httpHandler.handle(get(uri).header(HttpHeaders.ACCEPT, MediaType.TEXT_CSS).build());
                return response.entity().toString();
            } catch (Exception e) {
                return ExceptionRenderer.toString(e);
            }
        }
    }
}
