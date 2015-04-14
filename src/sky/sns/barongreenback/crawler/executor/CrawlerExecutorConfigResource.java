package sky.sns.barongreenback.crawler.executor;

import sky.sns.barongreenback.shared.messages.Category;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.FormParameters;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import static sky.sns.barongreenback.crawler.executor.CrawlerConfigValues.fromDisplayName;
import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Path("crawler/executor")
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
public class CrawlerExecutorConfigResource {

    private final CrawlerExecutors crawlerExecutors;
    private final Redirector redirector;

    public CrawlerExecutorConfigResource(CrawlerExecutors crawlerExecutors, Redirector redirector) {
        this.crawlerExecutors = crawlerExecutors;
        this.redirector = redirector;
    }

    @GET
    @Path("list")
    public Model list(@QueryParam("message") Option<String> message, @QueryParam("category") Option<Category> category) {
        final Model model = model().add("InputHandlerThreads", crawlerExecutors.inputHandlerThreads()).
                add("InputHandlerCapacity", crawlerExecutors.inputHandlerCapacity()).
                add("ProcessHandlerThreads", crawlerExecutors.processHandlerThreads()).
                add("ProcessHandlerCapacity", crawlerExecutors.processHandlerCapacity()).
                add("OutputHandlerThreads", crawlerExecutors.outputHandlerThreads()).
                add("OutputHandlerCapacity", crawlerExecutors.outputHandlerCapacity());
        if (message.isDefined() && category.isDefined()) {
            return model.add("message", model().add("text", message.get()).add("category", category.get()));
        }
        return model;
    }

    @POST
    @Path("update")
    public Response update(@FormParam("formParams") final FormParameters formParameters) {
        Sequence<Pair<String, Either<Exception, Integer>>> map = Sequences.sequence(formParameters).filter(where(Callables.<String>first(), not("action"))).map(Callables.<String, String, Either<Exception, Integer>>second(toInteger().orException()));
        Sequence<Pair<String, Either<Exception, Integer>>> exceptions = map.filter(where(Callables.<Either<Exception, Integer>>second(), Predicates.isLeft()));
        Sequence<Pair<String, Either<Exception, Integer>>> configValues = map.filter(where(Callables.<Either<Exception, Integer>>second(), Predicates.isRight()));

        if (exceptions.isEmpty()) {
            Sequence<Pair<String, Integer>> values = configValues.map(toValues());
            Sequence<Pair<CrawlerConfigValues, Integer>> map1 = values.map(toParameter());
            crawlerExecutors.handlerValues(map1);
            return redirector.seeOther(method(on(CrawlerExecutorConfigResource.class).list(some("Executor Config Updated"), some(Category.SUCCESS))));
        }
        return redirector.seeOther(method(on(CrawlerExecutorConfigResource.class).list(some("Error Updating Config Data"), some(Category.ERROR))));
    }

    private Callable1<Pair<String, Integer>, Pair<CrawlerConfigValues, Integer>> toParameter() {
        return new Callable1<Pair<String, Integer>, Pair<CrawlerConfigValues, Integer>>() {
            @Override
            public Pair<CrawlerConfigValues, Integer> call(Pair<String, Integer> stringIntegerPair) throws Exception {
                return Pair.pair(fromDisplayName(stringIntegerPair.first()), stringIntegerPair.second());
            }
        };
    }

    private Function1<Pair<String, Either<Exception, Integer>>, Pair<String, Integer>> toValues() {
        return Callables.second(Callables.<Integer>right());
    }

    private Function1<String, Integer> toInteger() {
        return new Function1<String, Integer>() {
            @Override
            public Integer call(String value) throws Exception {
                return Integer.valueOf(value);
            }
        };
    }
}
