package com.googlecode.barongreenback.search.pager;

import com.googlecode.funclate.Renderer;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.totallylazy.URLs;

import java.util.HashMap;
import java.util.Map;

public class PagerRenderer implements Renderer<Pager> {
    public static PagerRenderer pagerRenderer() {
        return new PagerRenderer();
    }

    public String render(Pager pager) throws Exception {
        PagerModel model = new PagerModel();

        EnhancedStringTemplateGroup stringTemplateGroup = new EnhancedStringTemplateGroup(URLs.packageUrl(this.getClass()));
        stringTemplateGroup.enableFormatsAsFunctions();
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("pages", model.pages(pager));
        return stringTemplateGroup.getInstanceOf("pager", attributes).toString();
    }


}
