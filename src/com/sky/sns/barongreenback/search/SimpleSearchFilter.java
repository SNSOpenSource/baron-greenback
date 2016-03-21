package com.sky.sns.barongreenback.search;

public class SimpleSearchFilter implements SearchFilter {

    private final String filter;

    public SimpleSearchFilter(String filter)
    {
        this.filter = filter;
    }

    @Override
    public String getFilter() {
        return filter;
    }
}
