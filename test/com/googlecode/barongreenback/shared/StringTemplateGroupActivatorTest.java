package com.googlecode.barongreenback.shared;

import com.googlecode.utterlyidle.MatchedResource;
import org.antlr.stringtemplate.StringTemplate;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class StringTemplateGroupActivatorTest {
    @Test
    public void groupTemplatesShouldDashSpacedNames() throws Exception {
        StringTemplateGroupActivator activator = new StringTemplateGroupActivator(new MatchedResource(HomeResource.class), new Properties());
        StringTemplate template = activator.call().getInstanceOf("group");
        template.setAttribute("name", "string with spaces");
        assertThat(template.toString(), containsString("string-with-spaces"));
    }

    @Test
    public void shouldReplaceSpacesWithUnderscores() throws Exception {
        StringTemplateGroupActivator activator = new StringTemplateGroupActivator(new MatchedResource(StringTemplateGroupActivatorTest.class), new Properties());
        StringTemplate template = activator.call().getInstanceOf("replaceWithUnderscores");
        template.setAttribute("name", "string with spaces");
        assertThat(template.toString(), containsString("string_with_spaces"));
    }

    @Test
    public void shouldReplaceSpacesWithDashes() throws Exception {
        StringTemplateGroupActivator activator = new StringTemplateGroupActivator(new MatchedResource(StringTemplateGroupActivatorTest.class), new Properties());
        StringTemplate template = activator.call().getInstanceOf("replaceWithDashes");
        template.setAttribute("name", "string with spaces");
        assertThat(template.toString(), containsString("string-with-spaces"));
    }
}
