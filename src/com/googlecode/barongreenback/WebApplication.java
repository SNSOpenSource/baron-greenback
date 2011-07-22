package com.googlecode.barongreenback;

import com.googlecode.utterlyidle.RestApplication;
import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.utterlyidle.modules.Module;

public class WebApplication extends RestApplication {
    public WebApplication() {
        super(new CrawlerModule());
    }

    public static void main(String[] args) throws Exception {
        new RestServer(new WebApplication(), ServerConfiguration.defaultConfiguration());
    }
}
