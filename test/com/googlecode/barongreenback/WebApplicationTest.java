package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Files;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.waitrest.Waitrest;
import org.junit.Ignore;

import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

@Ignore
public class WebApplicationTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("baron-greenback.lucene.index.directory", Files.TEMP_DIR + "/" + WebApplicationTest.class.getSimpleName());
        new Waitrest("/", 8899);
        new RestServer(new WebApplication(BasePath.basePath("/"), System.getProperties()), defaultConfiguration().port(9000));
    }
}
