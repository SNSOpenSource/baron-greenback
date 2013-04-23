package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;
import org.junit.Test;

import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;

public class CompositeLessCssCacheTest {
    @Test
    public void shouldTryAllCachesOnGet() throws Exception {
        InMemoryLessCssCache inMemory = new InMemoryLessCssCache();
        CachedLessCss cached = new CachedLessCss("more", date(1974, 10, 29));
        inMemory.put("cheese", cached);
        CompositeLessCssCache composite = CompositeLessCssCache.compositeLessCssCache(NoLessCssCache.instance, inMemory);
        Option<CachedLessCss> cheese = composite.get("cheese");
        assertThat(cheese, is(some(cached)));
    }

    @Test
    public void shouldTryAllCachesOnPut() throws Exception {
        InMemoryLessCssCache inMemory = new InMemoryLessCssCache();
        CachedLessCss cached = new CachedLessCss("more", date(1974, 10, 29));
        CompositeLessCssCache composite = CompositeLessCssCache.compositeLessCssCache(NoLessCssCache.instance, inMemory);
        boolean result = composite.put("cheese", cached);
        assertThat(result, is(true));
        assertThat(inMemory.get("cheese"), is(some(cached)));
    }
}
