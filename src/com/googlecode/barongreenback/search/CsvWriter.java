package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.shared.RenderableTypes;
import com.googlecode.funclate.Renderer;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordTo;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;

import java.io.Writer;
import java.util.List;

import static com.googlecode.funclate.Renderer.constructors.renderer;
import static com.googlecode.funclate.Renderer.functions.render;
import static com.googlecode.lazyrecords.Keyword.functions.name;
import static com.googlecode.totallylazy.Callables.toString;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CsvWriter {
    public static final char ROW_SEPARATOR = '\n';
    private static final String FIELD_SEPARATOR = ",";

    private final RenderableTypes renderableTypes;

    public CsvWriter(RenderableTypes renderableTypes) {
        this.renderableTypes = renderableTypes;
    }

    public void writeTo(Sequence<Record> records, Writer writer, Sequence<? extends Keyword<?>> fields) throws Exception {
        final Sequence<String> csvContent = sequence(headers(fields)).join(records.map(rowToString(fields)));
        csvContent.forEach(writeLine(writer));
    }

    private Block<String> writeLine(final Writer writer) {
        return new Block<String>() {
            @Override
            protected void execute(String line) throws Exception {
                writer.append(line).append(ROW_SEPARATOR);
            }
        };
    }

    private RecordTo<String> rowToString(final Sequence<? extends Keyword<?>> fields) {
        return new RecordTo<String>() {
            @Override
            public String call(final Record record) throws Exception {
                return fields.map(renderField(record)).map(escapeSpecialCharacters()).toString(FIELD_SEPARATOR);
            }
        };
    }

    private Callable1<Keyword<?>, String> renderField(final Record record) {
        return new Callable1<Keyword<?>, String>() {
            @Override
            public String call(Keyword<?> keyword) throws Exception {
                final Renderer renderer = rendererFor(keyword);
                final Callable1<Object, String> renderField = render(renderer);
                return option(record.get(keyword)).map(renderField).getOrElse("");
            }
        };
    }

    private Renderer rendererFor(Keyword<?> keyword) {
        final List<Pair<Class<?>, Renderer<?>>> types = renderableTypes.renderableTypes();
        final Option<Pair<Class<?>, Renderer<?>>> rendererOption = sequence(types).find(where(Callables.<Class<?>>first(), Predicates.<Class<?>>is(keyword.forClass())));
        return rendererOption.map(Callables.<Renderer<?>>second()).getOrElse(renderer(toString));
    }

    private Function1<String, String> escapeSpecialCharacters() {
        return new Function1<String, String>() {
            @Override
            public String call(String recordValue) throws Exception {
                recordValue = recordValue.replace('\n', ' ');
                if (recordValue.contains(",")) {
                    return '"' + recordValue + '"';
                }
                return recordValue;
            }
        };
    }

    private String headers(Sequence<? extends Keyword<?>> fields) {
        return fields.map(name).toString(FIELD_SEPARATOR);
    }
}