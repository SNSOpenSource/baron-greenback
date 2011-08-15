package com.googlecode.barongreenback;

import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.utterlyidle.FormParameters;
import org.junit.Test;

import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XmlDefinitionExtractorTest {
    @Test
    public void correctlyExtractsFormParamters() throws Exception {
        FormParameters form = FormParameters.formParameters(pair(XmlDefinitionExtractor.ROOT_XPATH, "/feed/entry"),
                pair(XmlDefinitionExtractor.FIELDS, "name"),
                pair(XmlDefinitionExtractor.ALIASES, ""),
                pair(XmlDefinitionExtractor.TYPES, "java.lang.String"),
                pair(XmlDefinitionExtractor.UNIQUE, "false"),

                pair(XmlDefinitionExtractor.FIELDS, "id"),
                pair(XmlDefinitionExtractor.ALIASES, ""),
                pair(XmlDefinitionExtractor.TYPES, "java.lang.Integer"),
                pair(XmlDefinitionExtractor.UNIQUE, "true")

        );

        XmlDefinitionExtractor extractor = new XmlDefinitionExtractor(form);

        XmlDefinition definition = extractor.extract();
        Keyword<Object> keyword = keyword("/feed/entry");
        assertThat(definition.rootXPath(), is(keyword));
        assertThat(definition.allFields(), hasExactly(keyword("name", String.class), keyword("id", Integer.class)));
        assertThat(definition.uniqueFields(), hasExactly(keyword("id", Integer.class)));
    }
}
