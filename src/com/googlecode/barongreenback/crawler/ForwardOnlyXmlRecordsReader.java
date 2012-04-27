package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordsReader;
import com.googlecode.lazyrecords.xml.XmlSequence;
import com.googlecode.lazyrecords.xml.mappings.XmlMappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;

public class ForwardOnlyXmlRecordsReader implements RecordsReader {
    private final XmlMappings mappings;
    private Document document;

    public ForwardOnlyXmlRecordsReader(Document document) {
        this.document = document;
        this.mappings = new XmlMappings();
    }

    public Sequence<Record> get(Definition definition) {
        Sequence<Node> nodes = forwardOnlySelectNodes(document, definition.name());
        document = null;
        return new XmlSequence(nodes, mappings, definition.fields());
    }

    private Sequence<Node> forwardOnlySelectNodes(Document document, String name) {
        List<Node> nodes = Xml.selectNodes(document, name).toList();
        return Sequences.forwardOnly(new PoppingIterator<Node>(nodes.iterator()));
    }

}
