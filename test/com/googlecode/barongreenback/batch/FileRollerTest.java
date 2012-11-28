package com.googlecode.barongreenback.batch;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.time.Dates;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static com.googlecode.totallylazy.Files.lastModified;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FileRollerTest {
    @Test
    public void shouldDeleteOldestFiles() throws Exception {
        File dir = Files.emptyTemporaryDirectory("file-roller");
        fileLastModifiedAt(dir, date(2000, 1, 1, 12, 0, 0));
        File file2 = fileLastModifiedAt(dir, date(2000, 1, 1, 12, 1, 0));
        File file3 = fileLastModifiedAt(dir, date(2000, 1, 1, 12, 2, 0));

        assertThat(Files.files(dir).size(), is(3));

        new FileRoller(dir, 2).call();

        assertThat(Files.files(dir).size(), is(2));
        assertThat(Files.files(dir).sortBy(lastModified()), is(sequence(file2, file3).sortBy(lastModified())));
    }

    private File fileLastModifiedAt(File dir, Date date) {
        File file = Files.temporaryFile(dir);
        file.setLastModified(date.getTime());
        return file;
    }
}