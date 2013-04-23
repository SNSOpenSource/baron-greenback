package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.time.Dates;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.HttpHeaders;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.ResponseBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.utterlyidle.RequestBuilder.get;
import static org.junit.Assert.fail;

public class LessCssHandlerTest {
    private InMemoryCompiledLessCache cache;

    @Before
    public void createHandler() throws Exception {
        cache = new InMemoryCompiledLessCache();
    }

    @Test
    public void usesCache() throws Exception {
        ensureCacheContains("something.less");
        try {
            new LessCssHandler(stubHandler(), exceptionThrowingCompiler(), alwaysCache(), cache).handle(get("something.less").build());
        } catch (RuntimeException e) {
            fail("Exception throwing compiler should not have been called");
        }
    }

    @Test(expected = RuntimeException.class)
    public void ignoresCacheWhenPurging() throws Exception {
        ensureCacheContains("something.less");
        Request request = get("something.less?purge").build();
        new LessCssHandler(stubHandler(), exceptionThrowingCompiler(), new CacheUnlessPurge(request), cache).handle(request);
        fail("Cache should have been ignored and compiler should have been called");
    }


    private void ensureCacheContains(final String path) throws Exception {
        cache.put(path, new CompiledLess("", Dates.date(2000, 1, 1)));
    }

    private LessCssConfig alwaysCache() {
        return new LessCssConfig() {
            @Override
            public boolean useCache() {
                return true;
            }
        };
    }

    private LessCompiler exceptionThrowingCompiler() {
        return new LessCompiler() {
            @Override
            public String compile(String less, LessCssHandler.Loader loader) throws IOException {
                throw new RuntimeException("This compiler likes to blow up");
            }
        };
    }

    private HttpHandler stubHandler() {
        return new HttpHandler() {
            @Override
            public Response handle(Request request) throws Exception {
                return ResponseBuilder.response().header(HttpHeaders.LAST_MODIFIED, Dates.date(2000, 1, 2)).build();
            }
        };
    }
}