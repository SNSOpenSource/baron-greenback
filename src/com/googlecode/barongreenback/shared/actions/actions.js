jQuery(document).ready(function() {
    var removeAllIdentifiers = function() {
        jQuery('div.actions form input[name="query"]').remove();
        jQuery('div.actions form input[name="id"]').remove();
    };

    var rowCount = function() {
        return jQuery('ul.tabs li.active a.tab span.count').text().replace(/[^0-9]/g, '');
    };

    jQuery("table.results tbody").click(function(event) {
        if (event.target.nodeName === 'TD') {
            var target = jQuery(event.target);
            var parent = jQuery(target.parent('tr')[0]);
            parent.toggleClass('selected');
            removeAllIdentifiers();
            jQuery('div.actions form').append(jQuery('table.results tbody tr.selected >td:first-child').map(function(index, el){return jQuery(el).text()}).map(function(index, text) {
                return '<input type="hidden" name="id" value="' + text + '"/>';
            }).toArray().join(''));
            jQuery('.actions .message').text(jQuery('table.results tr.selected').size() + " rows are selected");
        }
    });
    jQuery('.actions a.selectPage').click(function() {
        jQuery('table.results tbody tr').removeClass('selected');
        jQuery('table.results tbody td:first-child').click();
        return false;
    });
    jQuery('.actions a.selectAll').click(function() {
        jQuery('.actions a.selectPage').click();
        removeAllIdentifiers();
        jQuery('div.actions form').append('<input type="hidden" name="query" value="' + jQuery('meta[name="query"]').attr('content') + '">');
        jQuery('.actions .message').text('ALL ' + rowCount() +' rows are selected');
        return false;
    });
});