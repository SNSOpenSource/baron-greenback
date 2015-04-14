package com.googlecode.barongreenback.search;

import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.json.Json;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DrillDowns implements Value<Map<String, List<String>>> {

    private final Map<String, List<String>> drillDown;

    private DrillDowns(Map<String, List<String>> drillDown) {
        this.drillDown = drillDown;
    }

    public static DrillDowns drillDowns(Map<String, List<String>> drillDown) {
        return new DrillDowns(drillDown);
    }

    public static DrillDowns empty() {
        return drillDowns(Collections.<String, List<String>>emptyMap());
    }

    @Override
    public Map<String, List<String>> value() {
        return drillDown;
    }

    @Override
    public String toString() {
        return Json.json(drillDown);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DrillDowns that = (DrillDowns) o;

        return drillDown.equals(that.drillDown);

    }

    @Override
    public int hashCode() {
        return drillDown.hashCode();
    }
}
