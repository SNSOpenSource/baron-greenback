package com.googlecode.barongreenback.streamingtest;

import nu.xom.*;
import nux.xom.xquery.StreamingPathFilter;
import nux.xom.xquery.StreamingTransform;
import nux.xom.xquery.XQueryUtil;

import java.io.File;
import java.io.IOException;

public class NuxTest {

    public static void main(String[] args) throws IOException, ParsingException {
        StreamingTransform myTransform = new StreamingTransform() {
            @Override
            public Nodes transform(Element thing) {
//                Nodes results = XQueryUtil.xquery(thing, "name[../address/city = 'San Francisco']");
//                if (results.size() > 0) {
//                    System.out.println("name = " + results.get(0).getValue());
//                }
                System.out.println("Found a thing");
                return new Nodes(); // mark current element as subject to garbage collection
            }
        };

        // parse document with a filtering Builder
        Builder builder = new Builder(new StreamingPathFilter("/rss/channel/item", null)
                .createNodeFactory(null, myTransform));
        builder.build(NuxTest.class.getResourceAsStream("test.xml"));
    }
}
