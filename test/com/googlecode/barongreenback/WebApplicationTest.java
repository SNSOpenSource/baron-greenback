package com.googlecode.barongreenback;

import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.waitrest.Restaurant;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;

@Ignore
public class WebApplicationTest {

    public static void main(String[] args) throws Exception {
        new RestServer(new Restaurant(), ServerConfiguration.defaultConfiguration().port(8899));
        new RestServer(new WebApplication(), defaultConfiguration().port(9000));
    }
}
