package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.FormParameters;
import org.junit.Test;

import java.util.List;

import static com.googlecode.totallylazy.Pair.pair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ParametersToModelTest {
    @Test
    public void canConvertRootElements() throws Exception {
        FormParameters parameters = FormParameters.formParameters(pair("foo", "bar"), pair("baz", "dan"));
        Model model = ParametersToModel.modelOf(parameters);
        assertThat(model.get("foo", String.class), is("bar"));
        assertThat(model.get("baz", String.class), is("dan"));
    }

    @Test
    public void canConvert1LevelNestedForm() throws Exception {
        FormParameters parameters = FormParameters.formParameters(pair("parent.foo", "bar"), pair("parent.baz", "dan"));
        Model model = ParametersToModel.modelOf(parameters).get("parent", Model.class);
        assertThat(model.get("foo", String.class), is("bar"));
        assertThat(model.get("baz", String.class), is("dan"));
    }

    @Test
    public void canConvert2LevelNestedForm() throws Exception {
        FormParameters parameters = FormParameters.formParameters(pair("grandparent.parent.foo", "bar"), pair("grandparent.parent.baz", "dan"));
        Model model = ParametersToModel.modelOf(parameters).
                get("grandparent", Model.class).
                get("parent", Model.class);
        assertThat(model.get("foo", String.class), is("bar"));
        assertThat(model.get("baz", String.class), is("dan"));
    }

    @Test
    public void canConvertListsNestedForm() throws Exception {
        FormParameters parameters = FormParameters.formParameters(pair("list[1].foo", "bar"), pair("list[1].baz", "dan"),
                pair("list[2].foo", "matt"), pair("list[2].baz", "bob"));
        List<Model> list = ParametersToModel.modelOf(parameters).
                getValues("list", Model.class);
        assertThat(list.get(0).get("foo", String.class), is("bar"));
        assertThat(list.get(0).get("baz", String.class), is("dan"));
        assertThat(list.get(1).get("foo", String.class), is("matt"));
        assertThat(list.get(1).get("baz", String.class), is("bob"));
    }

    @Test
    public void canConvertTrueAndFalse() throws Exception {
        FormParameters parameters = FormParameters.formParameters(pair("foo", "true"), pair("baz", "false"));
        Model model = ParametersToModel.modelOf(parameters);
        assertThat(model.get("foo", Boolean.class), is(true));
        assertThat(model.get("baz", Boolean.class), is(false));
    }

    @Test
    public void canConvertRealExample() throws Exception {
        FormParameters parameters = FormParameters.parse("form.update=news&form.from=http%3A%2F%2Ffeeds.bbci.co.uk%2Fnews%2Frss.xml&form.record.name=%2Frss%2Fchannel%2Fentry&form.record.keywords%5B1%5D.name=title&form.record.keywords%5B1%5D.alias=&form.record.keywords%5B1%5D.group=&form.record.keywords%5B1%5D.type=java.lang.String&form.record.keywords%5B1%5D.unique=false&form.record.keywords%5B1%5D.visible=true&form.record.keywords%5B1%5D.visible=false&form.record.keywords%5B1%5D.subfeed=false&form.record.keywords%5B1%5D.subfeedPrefix=form.record.keywords%5B1%5D.record.&form.record.keywords%5B2%5D.name=description&form.record.keywords%5B2%5D.alias=&form.record.keywords%5B2%5D.group=&form.record.keywords%5B2%5D.type=java.lang.String&form.record.keywords%5B2%5D.unique=false&form.record.keywords%5B2%5D.visible=true&form.record.keywords%5B2%5D.visible=false&form.record.keywords%5B2%5D.subfeed=false&form.record.keywords%5B2%5D.subfeedPrefix=form.record.keywords%5B2%5D.record.&form.record.keywords%5B3%5D.name=link&form.record.keywords%5B3%5D.alias=&form.record.keywords%5B3%5D.group=&form.record.keywords%5B3%5D.type=java.lang.String&form.record.keywords%5B3%5D.unique=false&form.record.keywords%5B3%5D.visible=true&form.record.keywords%5B3%5D.visible=false&form.record.keywords%5B3%5D.subfeed=true&form.record.keywords%5B3%5D.subfeed=false&form.record.keywords%5B3%5D.subfeedPrefix=form.record.keywords%5B3%5D.record.&form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.name=&form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.alias=&form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.group=&form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.type=java.lang.String&form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.unique=false&form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.visible=true&form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.visible=false&form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.subfeed=false&form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.subfeedPrefix=form.record.keywords%5BKEYWORD_ID_REPLACE_ME%5D.record.&action=Save");
        Model model = ParametersToModel.modelOf(parameters);
        Model formModel = model.get("form", Model.class);
        assertThat(formModel.get("update", String.class), is("news"));
        assertThat(formModel.get("from", String.class), is("http://feeds.bbci.co.uk/news/rss.xml"));
    }
}
