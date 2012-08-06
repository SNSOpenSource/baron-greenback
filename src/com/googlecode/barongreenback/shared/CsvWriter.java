package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.search.RecordsService;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Sequence;

import java.io.Writer;

import static com.googlecode.totallylazy.Callables.toString;

public class CsvWriter {
    private static final String FIELD_SEPARATOR = ", ";
    private static final char ROW_SEPARATOR = '\n';

    public static void writeTo(Sequence<Record> records, Writer writer, Model view) {
        records.map(fieldsToString(view)).cons(headers(view)).fold(writer, writeLine());
    }

    private static Function2<Writer, String, Writer> writeLine() {
        return new Function2<Writer, String, Writer>() {
            @Override
            public Writer call(Writer writer, String line) throws Exception {
                return writer.append(line).append(ROW_SEPARATOR);
            }
        };
    }

    private static Function1<Record, String> fieldsToString(final Model view) {
        return new Function1<Record, String>() {
            @Override
            public String call(Record record) throws Exception {
                return record.getValuesFor(RecordsService.visibleHeaders(view)).map(toString).map(escapeSpecialCharacters()).toString(FIELD_SEPARATOR);
            }
        };
    }

    private static String headers(Model view) {
        return RecordsService.visibleHeaders(view).toString(FIELD_SEPARATOR);
    }

    private static Function1<String, String> escapeSpecialCharacters() {
        return new Function1<String, String>() {
            @Override
            public String call(String recordValue) throws Exception {
                recordValue = recordValue.replace("\n", " ");
                if (recordValue.contains(",")) {
                    recordValue = "\"" + recordValue + "\"";
                }
                return recordValue;
            }
        };
    }
}
