BGB.namespace('tables.dataTables').init = function () {
    $.fn.dataTableExt.oSort['string-pre'] = function (value) {
        if (typeof value != "string")  value = value !== null && value.toString ? value.toString() : "";
        return value;
    }

    Date.setLocale("en-UK");

    $.fn.dataTableExt.aTypes.unshift(function (sData) {
        if (sData !== null && !sData.toString().match(/^\d+$/) && Date.create && Date.create(sData).isValid()) {
            return 'sugar';
        }
        return null;
    });

    jQuery.fn.dataTableExt.aTypes.unshift(function (sData) {
        sData = typeof sData.replace == 'function' ?
            sData.replace(/<[\s\S]*?>/g, "") : sData;
        sData = $.trim(sData);

        var sValidFirstChars = "0123456789-";
        var sValidChars = "0123456789.";
        var Char;
        var bDecimal = false;

        /* Check for a valid first char (no period and allow negatives) */
        Char = sData.charAt(0);
        if (sValidFirstChars.indexOf(Char) == -1) {
            return null;
        }

        /* Check all the other characters are valid */
        for (var i = 1; i < sData.length; i++) {
            Char = sData.charAt(i);
            if (sValidChars.indexOf(Char) == -1) {
                return null;
            }

            /* Only allowed one decimal place... */
            if (Char == ".") {
                if (bDecimal) {
                    return null;
                }
                bDecimal = true;
            }
        }

        return 'num-html';
    });

    $.extend($.fn.dataTableExt.oSort, {
        "sugar-pre": function (a) {
            return $.trim(a) == '' ? 0 : Date.create($.trim(a)).getTime();
        },

        "sugar-asc": function (a, b) {
            return a - b;
        },

        "sugar-desc": function (a, b) {
            return b - a;
        },
        "num-html-pre": function (a) {
            var x = String(a).replace(/<[\s\S]*?>/g, "");
            return parseFloat(x);
        },

        "num-html-asc": function (a, b) {
            return ((a < b) ? -1 : ((a > b) ? 1 : 0));
        },

        "num-html-desc": function (a, b) {
            return ((a < b) ? 1 : ((a > b) ? -1 : 0));
        }
    });

    $.fn.dataTableExt.oStdClasses['sSortAsc'] = 'headerSortDown';
    $.fn.dataTableExt.oStdClasses['sSortDesc'] = 'headerSortUp';
}

BGB.namespace('tables').init = function () {
    if ($('table.results').length == 0) {
        return;
    }

    var currentSort = $('th.headerSortUp, th.headerSortDown').map(function (collectionIndex, elem) {
        return [$(elem).index(), $(elem).hasClass('headerSortUp') ? 'desc' : 'asc'];
    });

    var clientSideSort = $('table.results.paged').length == 0;
    var resultsTable = $('table.results').dataTable({
        'bPaginate': false,
        'bFilter': false,
        'bSort': clientSideSort,
        'aaSorting': [currentSort.length == 0 ? [0, 'asc'] : currentSort],
        'bInfo': false,
        'bAutoWidth': false,
        'bSortClasses': false
    });

    $('table.results:not(.paged) th a').click(function (event) {
        event.stopPropagation();
    });

    new FixedHeader(resultsTable, { offsetTop: $('.navbar').height()});
    $('div.dataTables_wrapper').css('position', '');
    var startingTop = $('table.results').position().top;

    $('div.fixedHeader').css({
        'position': 'absolute',
        'top': startingTop,
        'left': 'auto'
    });



    $('div.dataTables_wrapper').after($('div.fixedHeader').detach());

    var updateFixedHeaderPosition = function () {
        var navbarHeight = $('.navbar').height();

        var scrolled = $(window).scrollTop();
        var resultsTop = $('div.dataTables_wrapper').offset().top;
        var offsetAfterScroll = (resultsTop - scrolled);

        var newTop = $('div.dataTables_wrapper').position().top;
        if (offsetAfterScroll < navbarHeight) {
            newTop += navbarHeight - offsetAfterScroll;
        }

        $('div.fixedHeader').css({
            'position': 'absolute',
            'top': newTop + 'px',
            'left': 'auto'
        });
    };

    $(window).scroll(updateFixedHeaderPosition);

    $(window).resize(function () {
        if ($(".FixedHeader_Header").length == 0) {
            return;
        }

        var tableWidth = $("table.results").outerWidth();

        $(".FixedHeader_Header").width(tableWidth);
        $(".FixedHeader_Header > table").width(tableWidth);

        var columnsWidths = [];
        $('.dataTables_wrapper th').each(function () {
            columnsWidths.push($(this).width());
        });

        var fixedHeaderCells = $('.FixedHeader_Header th')
        for (var count = 0; count < columnsWidths.length; count++) {
            $(fixedHeaderCells[count]).css('width', columnsWidths[count] + 'px');
        }
        updateFixedHeaderPosition();
    });
}

$(document).ready(BGB.tables.dataTables.init);
$(document).ready(BGB.tables.init);
