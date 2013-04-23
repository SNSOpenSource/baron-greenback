package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.HttpClient;

import static com.googlecode.totallylazy.None.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.time.Dates.parse;
import static com.googlecode.utterlyidle.HttpHeaders.LAST_MODIFIED;

public class UriCompiledLessCache implements CompiledLessCache {
    private final Uri root;
    private final HttpClient httpClient;

    public UriCompiledLessCache(Uri root, HttpClient httpClient) {
        this.root = root;
        this.httpClient = httpClient;
    }

    @Override
    public Option<CompiledLess> get(String key) {
        try {
            Response response = httpClient.handle(RequestBuilder.get(cached(key)).build());
            if (response.status().isSuccessful()) {
                return some(new CompiledLess(response.entity().toString(), parse(response.headers().getValue(LAST_MODIFIED))));
            }
        } catch (Exception ignore) {
        }
        return none();
    }

    private Uri cached(String key) {return root.mergePath(key + ".css");}

    @Override
    public boolean put(String key, CompiledLess result) {
        try {
            Request request = RequestBuilder.put(cached(key)).
                    header(LAST_MODIFIED, result.lastModified()).
                    entity(result.less()).
                    build();
            return httpClient.handle(request).status().isSuccessful();
        } catch (Exception ignore) {
            return false;
        }
    }
}
