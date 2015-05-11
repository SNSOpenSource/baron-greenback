package com.sky.sns.barongreenback.shared;

import com.googlecode.funclate.Renderer;
import com.googlecode.totallylazy.Xml;

import java.net.URI;

public class URIRenderer implements Renderer<URI> {
    public static Renderer<URI> toLink() {
        return new URIRenderer();
    }

    public String render(URI uri) throws Exception {
        return String.format("<a href=\"%1$s\">%1$s</a>", Xml.escape(uri));
    }
}
