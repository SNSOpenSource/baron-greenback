package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.HttpHeaders;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.ResponseBuilder;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.rendering.ExceptionRenderer;

import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.utterlyidle.RequestBuilder.get;

public class LessCssHandler implements HttpHandler {
    private final LessCssCache cache;
    private final HttpHandler httpHandler;
    private final LessCompiler lessCompiler;
    private final LessCssConfig config;

    public LessCssHandler(HttpHandler httpHandler, LessCompiler lessCompiler, LessCssConfig config, LessCssCache cache) {
        this.httpHandler = httpHandler;
        this.lessCompiler = lessCompiler;
        this.config = config;
        this.cache = cache;
    }

    public Response handle(Request request) throws Exception {
        long start = System.currentTimeMillis();
        Response response = httpHandler.handle(request);

        Uri uri = request.uri();
        if (!(uri.path().endsWith(".less") && response.status().equals(Status.OK))) {
            return response;
        }
        String less = response.entity().toString();
        Response build = ResponseBuilder.modify(response).entity(processLess(uri, less)).build();
        System.out.println("Less Generation took :  " + (System.currentTimeMillis() - start));
        return build;
    }

    private String processLess(Uri uri, String less) throws IOException {
        if (cache.containsKey(uri) /*&& config.useCache()*/) {
            return cache.get(uri);
        }
        String result = lessCompiler.compile(less, new Loader(uri));
        cache.put(uri, result);
        return result;
    }

    public class Loader implements Callable1<String, String> {
        private Uri uri;

        public Loader(Uri uri) {
            this.uri = uri;
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
