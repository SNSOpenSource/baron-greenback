package com.googlecode.barongreenback.shared;

import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.utterlyidle.FormParameters;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.net.URI;

import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.ALIASES;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.FIELDS;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.RECORD_NAME;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.TYPES;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.UNIQUE;
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.VISIBLE;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RecordDefinitionExtractorTest {
    @Test
    public void correctlyExtractsFormParamters() throws Exception {
        FormParameters form = FormParameters.formParameters(pair(RECORD_NAME, "/feed/entry"),
                pair(FIELDS, "name"),
                pair(ALIASES, ""),
                pair(TYPES, "java.lang.String"),
                pair(UNIQUE, "false"),
                pair(VISIBLE, "false"),

                pair(FIELDS, "id"),
                pair(ALIASES, ""),
                pair(TYPES, "java.lang.Integer"),
                pair(UNIQUE, "true"), pair(UNIQUE, "false"),
                pair(VISIBLE, "false")

        );

        RecordDefinitionExtractor extractor = new RecordDefinitionExtractor(form);

        RecordDefinition definition = extractor.extract();
        Keyword<Object> keyword = keyword("/feed/entry");
        assertThat(definition.recordName(), is(keyword));
        assertThat(definition.fields(), hasExactly(keyword("name", String.class), keyword("id", Integer.class)));
        assertThat(uniqueFields(definition), hasExactly(keyword("id", Integer.class)));
    }

    @Test
    public void supportsSubFeed() throws Exception {
        String prefix = "subfeed1";

        FormParameters form = FormParameters.formParameters(
                pair(RECORD_NAME, "/feed/entry"),
                pair(FIELDS, "link"),
                pair(ALIASES, ""),
                pair(TYPES, "java.net.URI#" + prefix),
                pair(UNIQUE, "false"),
                pair(VISIBLE, "false"),

                pair(prefix + RECORD_NAME, "/user/summary"),
                pair(prefix + FIELDS, "ID"),
                pair(prefix + ALIASES, ""),
                pair(prefix + TYPES, "java.lang.Integer"),
                pair(prefix + UNIQUE, "true"), pair(prefix + UNIQUE, "false"),
                pair(prefix + VISIBLE, "false")

        );

        RecordDefinitionExtractor extractor = new RecordDefinitionExtractor(form);

        RecordDefinition definition = extractor.extract();
        Keyword<Object> keyword = keyword("/feed/entry");
        assertThat(definition.recordName(), is(keyword));
        assertThat(definition.fields(), hasExactly(keyword("link", URI.class)));

        Keyword<Object> subRoot = keyword("/user/summary");
        RecordDefinition subDefinition = definition.fields().head().metadata().get(RecordDefinition.XML_DEFINITION);
        assertThat(subDefinition.recordName(), is(subRoot));
        assertThat(subDefinition.fields(), hasExactly(keyword("id", Integer.class)));
        assertThat(uniqueFields(subDefinition), hasExactly(keyword("id", Integer.class)));

    }
}
