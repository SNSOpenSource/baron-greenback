package com.googlecode.barongreenback.shared.pager;

import com.googlecode.funclate.Model;
import com.googlecode.funclate.Renderer;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.URLs;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.totallylazy.Sequences.sequence;

public class PagerRenderer implements Renderer<Pager> {
    private StringTemplateGroup sharedGroup;

    public PagerRenderer(StringTemplateGroup sharedGroup) {
        this.sharedGroup = sharedGroup;
    }

    public static PagerRenderer pagerRenderer(StringTemplateGroup sharedGroup) {
        return new PagerRenderer(sharedGroup);
    }

    public String render(Pager pager) throws Exception {
        PagerModel model = new PagerModel();

        EnhancedStringTemplateGroup stringTemplateGroup = new EnhancedStringTemplateGroup(URLs.packageUrl(this.getClass()), sharedGroup);
        stringTemplateGroup.enableFormatsAsFunctions();
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("pages", model.pages(pager));
        attributes.put("pager", pager);

        Model pageOptions = sequence("10", "20", "50", "100", "ALL").fold(model().add("currentRowsPerPage", String.valueOf(pager.getRowsPerPage())), new Callable2<Model, String, Model>() {
            public Model call(Model model, String key) throws Exception {
                model.add("rowsPerPage", model().
                        add("name", key).
                        add("value", key).
                        add(key, true));
                return model;
            }
        });

        attributes.put("pageOptions", pageOptions.toMap());
        return stringTemplateGroup.getInstanceOf("pager", attributes).toString();
    }


}
