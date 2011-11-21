package com.googlecode.barongreenback.search.pager;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import org.junit.Test;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.numbers.Numbers.range;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PagerModelTest {

    @Test
    public void pagerModelHasNextAndPreviousPages() throws Exception {
        Pager pager = pagerWithValues(range(1, 101), 2, 20);

        Sequence<Page> pages = sequence(new PagerModel().pages(pager));
        assertThat(pages.head(), is(new Page("&#8592; Previous", "prev", "QS1")));
        assertThat(pages.last(), is(new Page("Next &#8594;", "next", "QS3")));
    }

    @Test
    public void previousButtonIsDisabledWhenNotApplicable() throws Exception {
        Pager pager = pagerWithValues(range(1, 101), 1, 20);

        Page previousPage = sequence(new PagerModel().pages(pager)).head();
        assertThat(previousPage.getCssClass(), containsString("disabled"));
        assertThat(previousPage.getLink(), is("#"));
    }

    @Test
    public void nextButtonIsDisabledWhenNotApplicable() throws Exception {
        Pager pager = pagerWithValues(range(1, 101), 5, 20);

        Page nextPage = sequence(new PagerModel().pages(pager)).last();
        assertThat(nextPage.getCssClass(), containsString("disabled"));
        assertThat(nextPage.getLink(), is("#"));
    }

    @Test
    public void showsAllPagesWhenFiveOrFewerPages() throws Exception {
        Pager pager = pagerWithValues(range(1, 101), 1, 20);

        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pager)).map(asText())), is(sequence("1", "2", "3", "4", "5")));
    }

    @Test
    public void showsPageSelectorForAPageInTheMiddle() throws Exception {
        Pager pager = pagerWithValues(range(1, 101), 50, 1);

        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pager)).map(asText())), is(sequence("1", "...", "49", "50", "51", "...", "100")));
    }

    @Test
    public void showsPageSelectorWhenOnLowPageNumber() throws Exception {
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 1, 10))).map(asText())), is(sequence("1", "2", "...", "100")));
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 2, 10))).map(asText())), is(sequence("1", "2", "3", "...", "100")));
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 3, 10))).map(asText())), is(sequence("1", "2", "3", "4", "...", "100")));
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 4, 10))).map(asText())), is(sequence("1", "2", "3", "4", "5", "...", "100")));
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 5, 10))).map(asText())), is(sequence("1", "...", "4", "5", "6", "...", "100")));
    }

    @Test
    public void showsPageSelectorForLastPage() throws Exception {
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 100, 10))).map(asText())), is(sequence("1", "...", "99", "100")));
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 99, 10))).map(asText())), is(sequence("1", "...", "98", "99", "100")));
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 98, 10))).map(asText())), is(sequence("1", "...", "97", "98", "99", "100")));
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 97, 10))).map(asText())), is(sequence("1", "...", "96", "97", "98", "99", "100")));
        assertThat(pageNumbersFrom(sequence(new PagerModel().pages(pagerWithValues(range(1, 1001), 96, 10))).map(asText())), is(sequence("1", "...", "95", "96", "97", "...", "100")));
    }


    private <T> Sequence<T> pageNumbersFrom(Sequence<T> pages) {
        return pages.tail().reverse().tail().reverse();
    }

    private Callable1<Page, String> asText() {
        return new Callable1<Page, String>() {
            public String call(Page page) throws Exception {
                return page.getText();
            }
        };
    }

    private Pager pagerWithValues(final Sequence<?> originalSequence, final int currentPage, final int rowsPerPage) {
        return new Pager() {
            public <T> Sequence<T> paginate(Sequence<T> sequence) {
                return (Sequence<T>) originalSequence.drop((getCurrentPage()-1) * getRowsPerPage()).take(getRowsPerPage());
            }

            public int getRowsPerPage() {
                return rowsPerPage;
            }

            public Number getTotalRows() {
                return originalSequence.size();
            }

            public int getCurrentPage() {
                return currentPage;
            }

            public Number getNumberOfPages() {
                return Math.ceil(getTotalRows().doubleValue() / getRowsPerPage());
            }

            public String getQueryStringForPage(int pageNumber) {
                return "QS"+pageNumber;
            }
        };
    }


}
