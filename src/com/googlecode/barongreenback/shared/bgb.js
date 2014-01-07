if (typeof BGB == 'undefined') {
    BGB = {};
}

BGB.namespace = function() {
    var a=arguments, o=null, i, j, d;
    for (i=0; i<a.length; i=i+1) {
        d=(""+a[i]).split(".");
        o=BGB;

        // BGB is implied, so it is ignored if it is included
        for (j=(d[0] == "BGB") ? 1 : 0; j<d.length; j=j+1) {
            o[d[j]]=o[d[j]] || {};
            o=o[d[j]];
        }
    }

    return o;
};

BGB.namespace('search').rowCount = function() {
    return parseInt(jQuery('ul.nav-tabs li.active a.tab span.count').text().replace(/[^0-9]/g, ''));
};

BGB.namespace('search').selectedRowCount = function() {
    return BGB.search.allPagesSelected
            ? BGB.search.rowCount()
            : jQuery('.actions >form:first-child input[name="id"]').length;
};

BGB.namespace('search').allPagesSelected = false;

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