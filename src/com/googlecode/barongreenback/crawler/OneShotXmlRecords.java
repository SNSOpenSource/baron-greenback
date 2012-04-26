package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.xml.XmlSequence;
import com.googlecode.lazyrecords.xml.mappings.XmlMappings;
import com.googlecode.totallylazy.Iterators;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.List;

public class OneShotXmlRecords extends AbstractRecords {

    private Document document;
    private final XmlMappings mappings;

    public OneShotXmlRecords(Document document, XmlMappings mappings) {
        this.document = document;
        this.mappings = mappings;
    }

    public OneShotXmlRecords(Document document) {
        this(document, new XmlMappings());
    }

    public Sequence<Record> get(Definition definition) {
        List<Node> nodes = Iterators.toList(Xml.selectNodes(document, definition.name()).iterator());
        document = null;
        return new XmlSequence(new KillingSequence<Node>(nodes.iterator()), mappings, definition.fields());
    }

    @Override
    public Number add(Definition definition, Sequence<Record> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        throw new UnsupportedOperationException();
    }

    public class KillingSequence<T> extends Sequence<T> {
        private Iterator<? extends T> iterator;

        public KillingSequence(final Iterator<? extends T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public T next() {
                    try {
                        return iterator.next();
                    } finally {
                        remove();
                    }
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }
    }
}
