package com.googlecode.barongreenback;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.PrintStreamLogger;
import com.googlecode.totallylazy.Files;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.waitrest.Waitrest;
import com.googlecode.yadic.Container;
import org.junit.Ignore;

import java.io.PrintStream;

import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

@Ignore
public class WebApplicationTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("baron-greenback.lucene.index.directory", Files.TEMP_DIR + "/" + WebApplicationTest.class.getSimpleName());
        new Waitrest("/", 8899);
        new RestServer(
                new WebApplication(BasePath.basePath("/"), System.getProperties()).
                        add(logRecordsTo(System.out)),
                defaultConfiguration().port(9000));
    }

    private static RequestScopedModule logRecordsTo(final PrintStream out) {
        return new RequestScopedModule() {
            @Override
            public Module addPerRequestObjects(Container container) throws Exception {
                container.remove(Logger.class);
                container.addInstance(Logger.class, new PrintStreamLogger(out));
                return null;
            }
        };
    }
}
