package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Uri.packageUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UriLessCssCacheTest {
    @Test
    public void shouldGetFromCache() throws Exception {
        UriLessCssCache cache = new UriLessCssCache(packageUri(UriLessCssCacheTest.class), new ClientHttpHandler());
        assertThat(cache.get("test-style.less").isEmpty(), is(false));
        assertThat(cache.get("not-found.less").isEmpty(), is(true));
    }

    @Test
    public void shouldPutIntoCache() throws Exception {
        Uri root = packageUri(UriLessCssCacheTest.class);
        UriLessCssCache cache = new UriLessCssCache(root, new ClientHttpHandler());
        Date lastModified = Dates.date(2000, 1, 1);
        if (Uri.JAR_URL.matches(root.toString())) {
            assertThat(cache.put("put.less", new CachedLessCss("", lastModified)), is(false));
        } else {
            File cacheFile = root.mergePath("put.less.css").toFile();
            delete(cacheFile);

            assertThat(cache.put("put.less", new CachedLessCss("", lastModified)), is(true));

            assertThat(cacheFile.exists(), is(true));
            assertThat(new Date(cacheFile.lastModified()), is(lastModified));
        }
    }
}