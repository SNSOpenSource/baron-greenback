package com.googlecode.barongreenback.crawler;


import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;

import java.util.UUID;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Some.some;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Path("crawler")
@Produces(MediaType.TEXT_HTML)
public class CrawlerImplementationResource {
    public static final Sequence<Pair<String,String>> CRAWLERS = sequence(
            pair("Sequential Crawler", SequentialCrawler.class.getName()),
            pair("Queues Crawler", QueuesCrawler.class.getName()));
    public static UUID ACTIVE_CRAWLER_ID = UUID.fromString("2efabd42-7961-4800-a5d4-8f974a3a2508");
    private final CrawlerActivator crawlerActivator;
    private final ModelRepository modelRepository;
    private final Redirector redirector;

    public CrawlerImplementationResource(CrawlerActivator crawlerActivator, ModelRepository modelRepository, Redirector redirector) {
        this.crawlerActivator = crawlerActivator;
        this.modelRepository = modelRepository;
        this.redirector = redirector;
    }

    @GET
    @Path("active")
    public Model active() throws ClassNotFoundException {
        return CRAWLERS.fold(model().add("activeCrawler", crawlerDisplayFor(currentCrawler())), toSelectDefinition());
    }

    private Callable2<Model, Pair<String, String>, Model> toSelectDefinition() {
        return new Callable2<Model, Pair<String, String>, Model>() {
            @Override
            public Model call(Model model, Pair<String, String> crawler) throws Exception {
                model.add("crawlers", model().add("name", crawler.first()).add("value", crawler.second()).add(crawler.first(), true));
                return model;
            }
        };
    }

    private String crawlerDisplayFor(String crawler) throws ClassNotFoundException {
        return CRAWLERS.filter(where(Callables.<String>second(), is(crawler))).map(Callables.<String>first()).head();
    }

    private String currentCrawler() throws ClassNotFoundException {
        return crawlerActivator.crawlerClass().getName();
    }

    @POST
    @Path("change")
    public Response changeCrawler(@FormParam("crawler") String crawler) {
        modelRepository.set(ACTIVE_CRAWLER_ID, model().add("crawler", crawler));
        return redirectToCrawlerList(some("Crawler changed to " + Sequences.sequence(crawler.split("\\.")).last()));
    }

    private Response redirectToCrawlerList(Option<String> messages) {
        return redirector.seeOther(method(on(CrawlerDefinitionResource.class).list(messages)));
    }

}
