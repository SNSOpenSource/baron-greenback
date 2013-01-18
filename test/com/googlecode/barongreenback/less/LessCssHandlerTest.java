package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static com.googlecode.utterlyidle.RequestBuilder.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class LessCssHandlerTest {

    private InMemoryLessCssCache cache;

    @Before
    public void createHandler() throws Exception {
        cache = new InMemoryLessCssCache();

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
        Request request = get(path).build();
        new LessCssHandler(stubHandler(), stubCompiler(), alwaysCache(), cache).handle(request);
        assertThat(cache.containsKey(request.uri().path()), is(true));
    }

    private LessCssConfig alwaysCache() {
        return new LessCssConfig() {
            @Override
            public boolean useCache() {
                return true;
            }
        };
    }

    private LessCompiler stubCompiler() {
        return new LessCompiler() {
            @Override
            public String compile(String less, LessCssHandler.Loader loader) throws IOException {
                return "";
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
                return ResponseBuilder.response().header(HttpHeaders.LAST_MODIFIED, new Date()).build();
            }
        };
    }

}
