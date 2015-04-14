package com.googlecode.barongreenback.shared;

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DateRendererTest {
    @Test
    public void shouldSupportCustomDateFormat() throws Exception {
        assertThat(DateRenderer.toLexicalDateTime("dd/MM/yy").render(new Date(0)), is("01/01/70"));
    }
}
