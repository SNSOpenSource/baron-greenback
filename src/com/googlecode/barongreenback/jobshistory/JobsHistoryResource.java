package com.googlecode.barongreenback.jobshistory;

import com.googlecode.barongreenback.shared.pager.Pager;
import com.googlecode.barongreenback.shared.sorter.Sorter;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.HttpMessageParser;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.QueryParam;

import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.action;
import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.elapsedTime;
import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.jobId;
import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.message;
import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.timestamp;
import static com.googlecode.barongreenback.shared.sorter.Sorter.sortKeywordFromRequest;
import static com.googlecode.funclate.Model.persistent.model;
import static com.googlecode.totallylazy.Strings.isBlank;
import static com.googlecode.utterlyidle.ResponseBuilder.response;

@Path("jobsHistory")
public class JobsHistoryResource {

    private JobsHistoryRepository jobsHistoryRepository;
    private final Pager pager;
    private final Sorter sorter;

    public JobsHistoryResource(JobsHistoryRepository jobsHistoryRepository, Pager pager, Sorter sorter) {
        this.jobsHistoryRepository = jobsHistoryRepository;
        this.pager = pager;
        this.sorter = sorter;
    }

    @GET
    @Path("list")
    public Model list(@QueryParam("query") @DefaultValue("") final String query) {
        Sequence<Keyword<?>> headers = JobHistoryItemDefinition.jobsHistory.fields();

        Sequence<Record> unpaged = jobsHistoryRepository.find(query);
        Sequence<Record> sorted = sorter.sort(unpaged, sortKeywordFromRequest(headers));
        Sequence<Record> paged = pager.paginate(sorted);

        Model model = model().add("query", query).add("items", paged.map(toModel()).toList());

        return pager.model(sorter.model(model, headers, paged));
    }

    @POST
    @Path("delete")
    public Response remove(@QueryParam("query") String query) {
        if (isBlank(query)) {
            return response(Status.BAD_REQUEST).build();
        }

        jobsHistoryRepository.remove(query);
        return response(Status.OK).build();
    }

    public static String queryForOlderThan(Integer hours) {
        return String.format("timestamp<\"$subtractHours(now,\"%d\")$\"", hours);
    }

    private Mapper<Record, Model> toModel() {
        return new Mapper<Record, Model>() {
            @Override
            public Model call(Record item) throws Exception {
                return asModel(item);
            }
        };
    }

    private Model asModel(final Record item) {
        return model().
                add("timestamp", item.get(timestamp)).
                add("elapsedTime", item.get(elapsedTime)).
                add("action", item.get(action)).
                add("jobId", item.get(jobId)).
                addOptionally("shortMessage", shortMessageFor(item)).
                addOptionally("rawMessage", item.get(message));
    }

    private String shortMessageFor(Record item) {
        final String rawMessage = item.get(message);

        if ("created".equalsIgnoreCase(item.get(action))) {
            return format(HttpMessageParser.parseRequest(rawMessage));
        } else if ("completed".equalsIgnoreCase(item.get(action))) {
            return format(HttpMessageParser.parseResponse(rawMessage));
        }

        return rawMessage;
    }

    private String format(Request request) {
        return String.format("%s %s %s", request.method(), request.uri(), request.entity());
    }

    private String format(Response response) {
        return String.format("%s %s", response.status(), response.entity());
    }
}
