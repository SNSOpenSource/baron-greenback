package com.sky.sns.barongreenback.crawler;

import com.sky.sns.barongreenback.crawler.datasources.DataSource;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import org.junit.Test;

import java.util.Set;

import static com.sky.sns.barongreenback.crawler.datasources.HttpDataSource.httpDataSource;
import static com.googlecode.totallylazy.Sequences.first;
import static com.googlecode.totallylazy.Uri.uri;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpVisitedFactoryTest {

    @Test
    public void canCreateVisitedFactoryWithSpecifiedSize() throws Exception {
        HttpVisitedFactory factory = HttpVisitedFactory.visitedFactory(1);
        Set<DataSource> dataSources = factory.value();
        dataSources.add(httpDataSource(uri("one"), someDefinition()));
        DataSource expectedDataSource = httpDataSource(uri("two"), someDefinition());
        dataSources.add(expectedDataSource);
        assertThat(dataSources.size(), is(1));
        assertThat(first(dataSources), is(expectedDataSource));
    }

    private Definition someDefinition() {
        return Definition.constructors.definition("something", Keyword.constructors.keyword("keyword"));
    }

}
