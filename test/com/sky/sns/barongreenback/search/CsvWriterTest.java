package sky.sns.barongreenback.search;

import sky.sns.barongreenback.shared.InMemoryRenderableTypes;
import com.googlecode.funclate.Renderer;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;
import org.junit.Test;

import java.io.StringWriter;

import static sky.sns.barongreenback.search.CsvWriter.ROW_SEPARATOR;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CsvWriterTest {

    private static final Keyword<String> FIELD_KEYWORD = keyword("field", String.class);
    private static final String FIELD_VALUE = "value";
    private static final Sequence<Record> RECORDS = sequence(record(FIELD_KEYWORD, FIELD_VALUE));

    @Test
    public void shouldRenderAFieldAsStringIfNotARenderableType() throws Exception {
        final CsvWriter csvWriter = new CsvWriter(new InMemoryRenderableTypes());
        final StringWriter writer = new StringWriter();

        csvWriter.writeTo(RECORDS, writer, sequence(FIELD_KEYWORD));

        assertThat(writer.toString(), is(outputCsv(FIELD_KEYWORD, FIELD_VALUE)));
    }

    @Test
    public void shouldRenderAFieldWithCustomRendererIfPresent() throws Exception {
        final Renderer<String> stringRenderer = bracketedStringRenderer();
        final CsvWriter csvWriter = new CsvWriter(new InMemoryRenderableTypes().add(String.class, stringRenderer));
        final StringWriter writer = new StringWriter();

        csvWriter.writeTo(sequence(RECORDS), writer, sequence(FIELD_KEYWORD));

        assertThat(writer.toString(), is(outputCsv(FIELD_KEYWORD, stringRenderer.render(FIELD_VALUE))));
    }

    @Test
    public void shouldEscapeCsvFieldsCorrectly() throws Exception {
        final CsvWriter csvWriter = new CsvWriter(new InMemoryRenderableTypes());
        final StringWriter writer = new StringWriter();

        final Record record = record(keyword("not_escaped", String.class), "not_escaped", keyword("comma_escaped", String.class), "comma,escaped", keyword("dquote_escaped", String.class), "dquote\"escaped");
        csvWriter.writeTo(sequence(record), writer, record.keywords());

        final String secondLine = writer.toString().split("\n")[1];
        assertThat(secondLine, is("not_escaped,\"comma,escaped\",\"dquote\"\"escaped\""));
    }

    private Renderer<String> bracketedStringRenderer() {
        return new Renderer<String>() {
            @Override
            public String render(String string) throws Exception {
                return format("[%s]", string);
            }
        };
    }

    private String outputCsv(Keyword<?> field, String value) throws Exception {
        return field.name() + ROW_SEPARATOR + value + ROW_SEPARATOR;
    }
}