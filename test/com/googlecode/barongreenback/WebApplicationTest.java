package com.googlecode.barongreenback;

import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.waitrest.Restaurant;
import org.junit.Ignore;

import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

@Ignore
public class WebApplicationTest {
    public static void main(String[] args) throws Exception {
        new RestServer(new Restaurant(), ServerConfiguration.defaultConfiguration().port(8899));
        new RestServer(new WebApplication(System.getProperties()), defaultConfiguration().port(9000));
    }
}
