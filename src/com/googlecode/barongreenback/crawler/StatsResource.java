package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Randoms;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;

import java.util.Date;
import java.util.List;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.numbers.Numbers.range;

@Path("stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatsResource {

    @GET
    @Path("crawler")
    public String cralwer() {
        List<Model> responseTimes = StatsCollector.stats().map(new Callable1<Pair<Long, Number>, Model>() {
            @Override
            public Model call(Pair<Long, Number> integerDatePair) throws Exception {
                return model().add("date", integerDatePair.first()).add("timing", integerDatePair.second());
            }
        }).take(100).toList();
        return model().add("responseTimes", responseTimes).toString();
    }
}
