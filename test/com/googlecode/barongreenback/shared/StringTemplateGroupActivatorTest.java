package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.WebApplicationTest;
import org.antlr.stringtemplate.StringTemplate;
import org.junit.Test;

import static com.googlecode.barongreenback.shared.StringTemplateGroupActivator.append;
import static com.googlecode.totallylazy.URLs.packageUrl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class StringTemplateGroupActivatorTest {
    @Test
    public void groupTemplatesShouldDashSpacedNames() throws Exception {
        StringTemplateGroupActivator activator = new StringTemplateGroupActivator(append(packageUrl(WebApplication.class), "shared"));
        StringTemplate template = activator.call().getInstanceOf("group");
        template.setAttribute("name", "string with spaces");
        assertThat(template.toString(), containsString("string-with-spaces"));
    }

    @Test
    public void shouldReplaceSpacesWithUnderscores() throws Exception {
        StringTemplateGroupActivator activator = new StringTemplateGroupActivator(append(packageUrl(WebApplicationTest.class), "shared"));
        StringTemplate template = activator.call().getInstanceOf("replaceWithUnderscores");
        template.setAttribute("name", "string with spaces");
        assertThat(template.toString(), containsString("string_with_spaces"));
    }

    @Test
    public void shouldReplaceSpacesWithDashes() throws Exception {
        StringTemplateGroupActivator activator = new StringTemplateGroupActivator(append(packageUrl(WebApplicationTest.class), "shared"));
        StringTemplate template = activator.call().getInstanceOf("replaceWithDashes");
        template.setAttribute("name", "string with spaces");
        assertThat(template.toString(), containsString("string-with-spaces"));
    }
}
