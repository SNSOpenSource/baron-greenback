jQuery(document).ready(function (event) {
    var expander = $("<div class='expander'></div>");
    $("body").append(expander);

    $(".shouldExpand td").each(function (i, node) {
        var elem = $(node);

        if (hasScrollBars(elem)) {
            elem.addClass("expandable");
        }

        if (elem.hasClass("expandable")) {
            elem.click(function (clickEvt) {
                expander.html(elem.html());
                expander.fadeIn();
                expander.css("top", expanderTopPosition(expander, elem));
                expander.css("left", expanderLeftPosition(expander, elem));

                $("html").one("click", function () {
                    expander.fadeOut();
                });
                return false;
            });
        }
    });

    function expanderTopPosition(expander, elem) {
        return Math.max(60, elem.offset().top - (expander.outerHeight() / 2) + (elem.outerHeight() / 2));
    }

    function expanderLeftPosition(expander, elem) {
        return elem.offset().left - (expander.outerWidth() / 2) + (elem.outerWidth() / 2);
    }

    function hasScrollBars(elem) {
        var cell = elem[0];
        return (cell.clientHeight < cell.scrollHeight) || (cell.clientWidth < cell.scrollWidth);
    }
});
