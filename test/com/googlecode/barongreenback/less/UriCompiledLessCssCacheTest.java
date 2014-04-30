package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.time.Dates;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Uri.packageUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UriCompiledLessCssCacheTest {
    @Test
    public void shouldGetFromCache() throws Exception {
        CompiledLessCache cache = new UriCompiledLessCache(packageUri(UriCompiledLessCssCacheTest.class));
        assertThat(cache.get("/test-style.less").isEmpty(), is(false));
        assertThat(cache.get("/not-found.less").isEmpty(), is(true));
    }

    @Test
    public void shouldPutIntoCache() throws Exception {
        Uri root = packageUri(UriCompiledLessCssCacheTest.class);
        CompiledLessCache cache = new UriCompiledLessCache(root);
        Date lastModified = Dates.date(2000, 1, 1);
        CompiledLess compiledLess = new CompiledLess("", lastModified);
        if (Uri.JAR_URL.matches(root.toString())) {
            assertThat(cache.put("/put.less", compiledLess), is(false));
        } else {
            File cacheFile = root.mergePath("put.less.css").toFile();
            System.out.println("cacheFile = " + cacheFile);
            delete(cacheFile);

            assertThat(cache.put("/put.less", compiledLess), is(true));

            assertThat(cacheFile.exists(), is(true));
            assertThat(new Date(cacheFile.lastModified()), is(lastModified));
        }
    }
}