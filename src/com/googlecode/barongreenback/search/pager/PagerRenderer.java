package com.googlecode.barongreenback.search.pager;

import com.googlecode.funclate.Renderer;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.URLs;
import com.googlecode.totallylazy.numbers.Numbers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PagerRenderer implements Renderer<Pager> {
    public static PagerRenderer pagerRenderer() {
        return new PagerRenderer();
    }

    public String render(Pager pager) throws Exception {
        List<Page> pages = new LinkedList<Page>();
        pages.add(getPreviousPage(pager));
        pages.addAll(pages(pager));
        pages.add(getNextPage(pager));

        EnhancedStringTemplateGroup stringTemplateGroup = new EnhancedStringTemplateGroup(URLs.packageUrl(this.getClass()));
        stringTemplateGroup.enableFormatsAsFunctions();
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("pages", pages);
        return stringTemplateGroup.getInstanceOf("pager", attributes).toString();
    }

    private Page getNextPage(Pager pager) {
        String buttonText = "Next &#8594;";
        if (pager.getCurrentPage() == pager.getNumberOfPages().intValue()) {
            return new Page(buttonText, "next disabled", "#");
        } else {
            return new Page(buttonText, "next", pager.getQueryStringForPage(pager.getCurrentPage() + 1));
        }
    }

    private Page getPreviousPage(Pager pager) {
        String buttonText = "&#8592; Previous";
        if (pager.getCurrentPage() == 1) {
            return new Page(buttonText, "prev disabled", "#");
        } else {
            return new Page(buttonText, "prev", pager.getQueryStringForPage(pager.getCurrentPage() - 1));
        }
    }

    public List<Page> pages(Pager pager) {
        return Numbers.range(1, pager.getNumberOfPages().intValue() + 1).map(toPage(pager)).toList();
    }

    private Callable1<? super Number, Page> toPage(final Pager pager) {
        return new Callable1<Number, Page>() {
            public Page call(Number number) throws Exception {
                String cssClass = "";
                if (number.intValue() == pager.getCurrentPage()) {
                    cssClass = "active";
                }
                return new Page(number.toString(), cssClass, pager.getQueryStringForPage(number.intValue()));
            }
        };
    }

    private class Page {
        private String text;
        private String cssClass;
        private String link;

        public Page(String text, String cssClass, String link) {
            this.text = text;
            this.cssClass = cssClass;
            this.link = link;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getText() {
            return text;
        }

        public String getLink() {
            return link;
        }
    }
}
