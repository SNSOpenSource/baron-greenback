package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.jobshistory.JobId;
import com.googlecode.barongreenback.jobshistory.JobsHistoryResource;
import com.googlecode.funclate.Renderer;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Redirector;

import java.net.URI;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static java.lang.String.format;

public class InMemoryRenderableTypesActivator implements Callable<RenderableTypes> {

    private final String dateFormat;
    private final Redirector redirector;

    public InMemoryRenderableTypesActivator(Properties properties, Redirector redirector) {
        this.dateFormat = properties.getProperty("barongreenback.date.format");
        this.redirector = redirector;
    }

    @Override
    public RenderableTypes call() throws Exception {
        return new InMemoryRenderableTypes().
                add(Date.class, dateFormat != null ? DateRenderer.toLexicalDateTime(dateFormat) : DateRenderer.toLexicalDateTime()).
                add(URI.class, URIRenderer.toLink()).
                add(JobId.class, new Renderer<JobId>() {
                    @Override
                    public String render(JobId jobId) throws Exception {
                        final String jobIdString = jobId.value().toString();
                        final Uri href = redirector.absoluteUriOf(method(on(JobsHistoryResource.class).list("jobId=\"" + jobIdString + "\"")));
                        return format("<a href=\"%s\">%s</a>", href, jobIdString);
                    }
                });
    }
}
