BGB.namespace('tables').init = (function () {

    var initDataTables = function () {
        $.fn.dataTableExt.oSort['string-pre'] = function (value) {
            if (typeof value != "string")  value = value !== null && value.toString ? value.toString() : "";
            return value;
        };

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
    };



    var registerAfterDrawCallback = function (table, funct) {
        var settings = table.fnSettings();
        settings.aoDrawCallback.unshift({
            'fn': funct,
            'sName': 'user'
        });
    };

    var initFixedHeader = function () {
        if (BGB.tables.interestedTables().length == 0) {
            return;
        }

        var currentSort = $('th.headerSortUp, th.headerSortDown').map(function (collectionIndex, elem) {
            return [$(elem).index(), $(elem).hasClass('headerSortUp') ? 'desc' : 'asc'];
        });

        var clientSideSort = BGB.tables.interestedTables().filter('.paged').length == 0;
        var resultsTable = BGB.tables.interestedTables().dataTable({
            'bPaginate': false,
            'bFilter': false,
            'bSort': clientSideSort,
            'aaSorting': [currentSort.length == 0 ? [0, 'asc'] : currentSort],
            'bInfo': false,
            'bAutoWidth': false,
            'bSortClasses': false
        });

        BGB.tables.interestedTables().filter(':not(.paged)').find('th a').click(function (event) {
            event.stopPropagation();
        });

        new FixedHeader(resultsTable, { offsetTop: $('.navbar').height()});

        /* This has to be done here because the FixedHeader plugin introduces some logic to reposition the header that we want to override. */
        registerAfterDrawCallback(resultsTable, BGB.tables.updateFixedHeaderPosition());


        $('div.dataTables_wrapper').css('position', '');
        var startingTop = BGB.tables.interestedTables().position().top;

        $('div.fixedHeader').css({
            'position': 'absolute',
            'top': startingTop,
            'left': 'auto'
        });

        $('div.dataTables_wrapper').after($('div.fixedHeader').detach());

        BGB.tables.updateFixedHeaderPosition();
    };

    var registerWindowEvents = function () {

        var lastVerticalScroll = $(window).scrollTop();
        $(window).scroll(function() {
            var verticalScrollAmount = $(window).scrollTop();
            if (verticalScrollAmount === lastVerticalScroll) {
                $('div.fixedHeader').css({
                    'position': 'absolute',
                    'left': 'auto'
                });
                return;
            }
            lastVerticalScroll = verticalScrollAmount;
            $('div.fixedHeader').hide();
            clearTimeout($.data(this, 'scrollTimer'));
            $.data(this, 'scrollTimer', setTimeout(BGB.tables.updateFixedHeaderPosition(), 500));
        });

        $(window).resize(BGB.tables.redrawFixedHeader);
    };

    return function () {
        initDataTables();
        initFixedHeader();
        registerWindowEvents();
    };
})();

BGB.namespace('tables').hideFixedHeader = function () {
    $('div.fixedHeader').hide();
};

BGB.namespace('tables').redrawFixedHeader = function () {

    function resizeHandler() {
        var fixedHeader = $("div.fixedHeader");
        if (fixedHeader.length == 0) {
            return;
        }

        var tableWidth = BGB.tables.interestedTables().outerWidth();

        fixedHeader.width(tableWidth);
        $("div.fixedHeader > table").width(tableWidth);

        var columnsWidths = [];
        $('.dataTables_wrapper th').each(function () {
            columnsWidths.push($(this).width());
        });

        var fixedHeaderCells = $('div.fixedHeader th')
        for (var count = 0; count < columnsWidths.length; count++) {
            $(fixedHeaderCells[count]).css('width', columnsWidths[count] + 'px');
        }
        BGB.tables.updateFixedHeaderPosition();
    }

    BGB.tables.hideFixedHeader();
    clearTimeout($.data(this, 'resizeTimer'));
    $.data(this, 'resizeTimer', setTimeout(resizeHandler, 500));
};

BGB.namespace('tables').updateFixedHeaderPosition = function () {
    var navbarHeight = $('.navbar').height();
    var dataTablesWrapper = $('div.dataTables_wrapper');

    var scrolled = $(window).scrollTop();
    var resultsTop = dataTablesWrapper.offset().top;
    var offsetAfterScroll = (resultsTop - scrolled);

    var newTop = dataTablesWrapper.position().top;
    if (offsetAfterScroll < navbarHeight) {
        newTop += navbarHeight - offsetAfterScroll;
    }

    var fixedHeader = $('div.fixedHeader');
    fixedHeader.css({
        'position': 'absolute',
        'top': newTop + 'px',
        'left': 'auto'
    });

    var lastRow = dataTablesWrapper.find('tr').last();
    if ((lastRow.offset().top - $(window).scrollTop()) > navbarHeight) {
        fixedHeader.slideDown('fast');
    }
};

BGB.namespace('tables').interestedTables = function() {
    return $('table.results:not(.static)');
};

$(document).ready(BGB.tables.init);
