package com.googlecode.barongreenback.search;

import com.googlecode.totallylazy.json.Json;
import com.googlecode.totallylazy.parser.Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class DrillDownsActivator implements Callable<DrillDowns> {

    private final DrillDowns drillDowns;

    public DrillDownsActivator(String drillDownQuery) {
        final Result<Map<String, List<String>>> parsedDrillDown = Json.parseMap(drillDownQuery.isEmpty() ? "{}" : drillDownQuery);
        if (parsedDrillDown.failure()) {
            throw new IllegalArgumentException(parsedDrillDown.message());
        }
        this.drillDowns = DrillDowns.drillDowns(parsedDrillDown.value());
    }

    @Override
    public DrillDowns call() throws Exception {
        return drillDowns;
    }
}
