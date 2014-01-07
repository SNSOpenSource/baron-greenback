$(document).ready(function() {
    $.fn.dataTableExt.oSort['string-pre'] = function (h) {
        if(typeof h != "string")  h = h !== null && h.toString ? h.toString() : "";
        return h;
    }

    $.fn.dataTableExt.oStdClasses['sSortAsc'] = 'headerSortDown';
    $.fn.dataTableExt.oStdClasses['sSortDesc'] = 'headerSortUp';

    var navbarHeight = $('.navbar').height();
    $('.content').css('top', navbarHeight + 'px');

    var currentSort = $('th.headerSortUp, th.headerSortDown').map(function(collectionIndex, elem){
        return [$(elem).index(), $(elem).hasClass('headerSortUp') ? 'desc' : 'asc'];
    });

    var clientSideSort = $('#results > table.paged').length == 0;
    var resultsTable = $('#results > table').dataTable({
        'bPaginate': false,
        'bSearchable' : false,
        'bFilter': false,
        'bSort': clientSideSort,
        'aaSorting': [currentSort || [0, 'asc']],
        'bInfo': false,
        'bAutoWidth': false,
        'sScrollY': '',
        'sScrollX': '',
        'bSortClasses': false
    });

    $('#results table th a').click(function (event) {
        event.stopPropagation();
    });

    new FixedHeader(resultsTable, { offsetTop: navbarHeight });
    $('.dataTables_wrapper').css('position', '');

    $('div.fixedHeader').css({
        'position': 'absolute',
        'top': '0px',
        'left': '0px'
    });

    $('#results').append($('div.fixedHeader').detach());

    var updateFixedHeaderPosition = function() {
        navbarHeight = $('.navbar').height();
        var resultsTop = $('#results').offset().top;
        var newTop;
        if (resultsTop < navbarHeight) {
            newTop = resultsTop < 0 ? (-resultsTop) + navbarHeight : navbarHeight - resultsTop;
        } else {
            newTop = 0;
        }
        $('div.fixedHeader').css({
            'top': newTop + 'px',
            'left': '0px'
        });
    }

    $('.content').scroll(updateFixedHeaderPosition);

    $(window).resize(function () {
        var tableWidth = $("table.results").outerWidth();

        $(".FixedHeader_Header").width(tableWidth);
        $(".FixedHeader_Header > table").width(tableWidth);

        var columnsWidths = [];
        $('.dataTables_wrapper th').each(function() {
            columnsWidths.push($(this).width());
        });

        var fixedHeaderCells = $('.FixedHeader_Header th')
        for (var count = 0; count < columnsWidths.length; count++) {
            $(fixedHeaderCells[count]).css('width', columnsWidths[count] + 'px');
        }
        updateFixedHeaderPosition();
    });
});