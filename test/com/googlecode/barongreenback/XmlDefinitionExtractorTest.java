package com.googlecode.barongreenback;

import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.utterlyidle.FormParameters;
import org.junit.Test;

import java.net.URI;

import static com.googlecode.barongreenback.XmlDefinition.uniqueFields;
import static com.googlecode.barongreenback.XmlDefinitionExtractor.ALIASES;
import static com.googlecode.barongreenback.XmlDefinitionExtractor.FIELDS;
import static com.googlecode.barongreenback.XmlDefinitionExtractor.ROOT_XPATH;
import static com.googlecode.barongreenback.XmlDefinitionExtractor.TYPES;
import static com.googlecode.barongreenback.XmlDefinitionExtractor.UNIQUE;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XmlDefinitionExtractorTest {

    @Test
    public void correctlyExtractsFormParamters() throws Exception {
        FormParameters form = FormParameters.formParameters(pair(ROOT_XPATH, "/feed/entry"),
                pair(FIELDS, "name"),
                pair(ALIASES, ""),
                pair(TYPES, "java.lang.String"),
                pair(UNIQUE, "false"),

                pair(FIELDS, "id"),
                pair(ALIASES, ""),
                pair(TYPES, "java.lang.Integer"),
                pair(UNIQUE, "true")

        );

        XmlDefinitionExtractor extractor = new XmlDefinitionExtractor(form);

        XmlDefinition definition = extractor.extract();
        Keyword<Object> keyword = keyword("/feed/entry");
        assertThat(definition.rootXPath(), is(keyword));
        assertThat(definition.fields(), hasExactly(keyword("name", String.class), keyword("id", Integer.class)));
        assertThat(uniqueFields(definition), hasExactly(keyword("id", Integer.class)));
    }

    @Test
    public void supportsSubFeed() throws Exception {
        String prefix = "subfeed1";

        FormParameters form = FormParameters.formParameters(
                pair(ROOT_XPATH, "/feed/entry"),
                pair(FIELDS, "link"),
                pair(ALIASES, ""),
                pair(TYPES, "java.net.URI#" + prefix),
                pair(UNIQUE, "false"),

                pair(prefix + ROOT_XPATH, "/user/summary"),
                pair(prefix + FIELDS, "ID"),
                pair(prefix + ALIASES, ""),
                pair(prefix + TYPES, "java.lang.Integer"),
                pair(prefix + UNIQUE, "true")

        );

        XmlDefinitionExtractor extractor = new XmlDefinitionExtractor(form);

        XmlDefinition definition = extractor.extract();
        Keyword<Object> keyword = keyword("/feed/entry");
        assertThat(definition.rootXPath(), is(keyword));
        assertThat(definition.fields(), hasExactly(keyword("link", URI.class)));

        Keyword<Object> subRoot = keyword("/user/summary");
        XmlDefinition subDefinition = definition.fields().head().metadata().get(XmlDefinition.XML_DEFINITION);
        assertThat(subDefinition.rootXPath(), is(subRoot));
        assertThat(subDefinition.fields(), hasExactly(keyword("id", Integer.class)));
        assertThat(uniqueFields(subDefinition), hasExactly(keyword("id", Integer.class)));

    }
}
