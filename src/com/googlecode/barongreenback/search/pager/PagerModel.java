package com.googlecode.barongreenback.search.pager;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.numbers.Numbers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.googlecode.totallylazy.numbers.Numbers.numbers;
import static com.googlecode.totallylazy.numbers.Numbers.range;

public class PagerModel {

    public static final int THRESHOLD_NUMBER_OF_PAGES_BEFORE_PARTITIONING = 5;

    private Page getNextPage(Pager pager) {
        String buttonText = "Next &#8594;";
        if (pager.getCurrentPage() == pager.getNumberOfPages().intValue()) {
            return new Page(buttonText, "next disabled", "#");
        } else {
            return new Page(buttonText, "next", pager.getQueryStringForPage(pager.getCurrentPage() + 1));
        }
    }

    private Page getPreviousPage(Pager pager) {
        String buttonText = "&#8592; Previous";
        if (pager.getCurrentPage() == 1) {
            return new Page(buttonText, "prev disabled", "#");
        } else {
            return new Page(buttonText, "prev", pager.getQueryStringForPage(pager.getCurrentPage() - 1));
        }
    }

    public List<Page> pages(Pager pager) {
        List<Page> pages = new LinkedList<Page>();
        pages.add(getPreviousPage(pager));
        pages.addAll(decorateSpaces(pageNumbers(pager), pager));
        pages.add(getNextPage(pager));
        return pages;
    }

    private List<Page> decorateSpaces(Sequence<Number> pageNumbers, Pager pager) {
        final List<Page> pagesWithEllipses = new ArrayList<Page>();

        Number last = pageNumbers.head();
        pagesWithEllipses.add(toPage(pager, last));
        
        List<Number> numbers = pageNumbers.tail().toList();

        for (Number pageNumber : numbers) {
            if (pageNumber.intValue() - last.intValue() > 1) {
                pagesWithEllipses.add(disabledPage());
            }
            pagesWithEllipses.add(toPage(pager, pageNumber.intValue()));
            last = pageNumber;
        }

        return pagesWithEllipses;
    }

    private Page disabledPage() {
        return new Page("...", "disabled", "#");
    }

    private Sequence<Number> pageNumbers(Pager pager) {
        if (pager.getNumberOfPages().intValue() < THRESHOLD_NUMBER_OF_PAGES_BEFORE_PARTITIONING + 1) {
            return Numbers.range(1, pager.getNumberOfPages().intValue() + 1);
        }

        if (pager.getCurrentPage() < THRESHOLD_NUMBER_OF_PAGES_BEFORE_PARTITIONING) {
            return range(1, pager.getCurrentPage()+2).join(numbers(pager.getNumberOfPages()));
        }

        if (pager.getCurrentPage() > (pager.getNumberOfPages().intValue() - (THRESHOLD_NUMBER_OF_PAGES_BEFORE_PARTITIONING-1))) {
            return numbers(1).join(range(pager.getCurrentPage() - 1, pager.getNumberOfPages().intValue() + 1));
        }

        return numbers(1).join(Numbers.range(pager.getCurrentPage() - 1, pager.getCurrentPage() + 2)).join(numbers(pager.getNumberOfPages().intValue()));
    }

    private Page toPage(final Pager pager, Number pageNumber) {
        String cssClass = "";
        if (pageNumber.intValue() == pager.getCurrentPage()) {
            cssClass = "active";
        }
        return new Page(pageNumber.toString(), cssClass, pager.getQueryStringForPage(pageNumber.intValue()));
    }

}
