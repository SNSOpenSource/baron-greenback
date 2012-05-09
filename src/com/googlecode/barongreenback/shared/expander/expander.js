jQuery(document).ready(function (event) {
    var expander = $("<div class='expander'></div>");
    $("body").append(expander);


    $(".shouldExpand td").each(function (i, node) {
        var elem = $(node);
        elem.click(buildExpanderClickHandler(elem));
        elem.hover(buildExpanderHoverOver(elem), buildExpanderHoverOut(elem));
    });

    function expanderTopPosition(expander, elem) {
        return Math.max(60, elem.offset().top - (expander.outerHeight() / 2) + (elem.outerHeight() / 2));
    }

    function expanderLeftPosition(expander, elem) {
        return elem.offset().left - (expander.outerWidth() / 2) + (elem.outerWidth() / 2);
    }

    function hasHiddenContent(elem) {
        if ($.browser.mozilla) {
            elem.css("display", "block");
        }

        var cell = elem[0];

        var hasHiddenContent = (cell.clientHeight < cell.scrollHeight) || (cell.clientWidth < cell.scrollWidth);

        if ($.browser.mozilla) {
            elem.css("display", "");
        }

        return hasHiddenContent;
    }
    function buildExpanderClickHandler(elem) {
        return function (clickEvt) {
            if(hasHiddenContent(elem)) {
                expander.html(elem.html());
                expander.fadeIn();
                expander.css("top", expanderTopPosition(expander, elem));
                expander.css("left", expanderLeftPosition(expander, elem));

                $("html").one("click", function () {
                    expander.fadeOut();
                });
                return false;
            }
        }
    }
    function buildExpanderHoverOver(elem) {
        return function() {
            if(hasHiddenContent(elem)) {
                elem.addClass("expandable");
            }
        }
    }
    function buildExpanderHoverOut(elem) {
        return function() {
            elem.removeClass("expandable");
        }
    }
});



