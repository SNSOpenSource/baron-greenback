package com.googlecode.barongreenback.shared;

import java.net.URI;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;

public class InMemoryRenderableTypesActivator implements Callable<RenderableTypes> {

    private final String dateFormat;

    public InMemoryRenderableTypesActivator(Properties properties) {
        this.dateFormat = properties.getProperty("barongreenback.date.format");
    }

    @Override
    public RenderableTypes call() throws Exception {
        return new InMemoryRenderableTypes().
                add(Date.class, dateFormat != null ? DateRenderer.toLexicalDateTime(dateFormat) : DateRenderer.toLexicalDateTime()).
                add(URI.class, URIRenderer.toLink());
    }
}
