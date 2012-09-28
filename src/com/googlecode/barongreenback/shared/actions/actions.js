(function() {
    var actions = BGB.namespace('search.bulk.actions');
    actions.allIdentifiers = function() {
        return jQuery('div.actions form input').filter('[name="id"], [name="query"]');
    };
    actions.removeAllIdentifiers = function() {
        jQuery(actions.allIdentifiers()).remove();
    };
})();


jQuery(document).ready(function() {
    jQuery('table.results tbody').click(function(event) {
        if (event.target.nodeName === 'TD') {
            var target = jQuery(event.target);
            var parent = jQuery(target.parent('tr')[0]);
            parent.toggleClass('selected');
            BGB.search.bulk.actions.removeAllIdentifiers();
            jQuery('div.actions form').append(jQuery('table.results tbody tr.selected >td:first-child').map(function(index, el){return jQuery(el).text()}).map(function(index, text) {
                return '<input type="hidden" name="id" value="' + text + '"/>';
            }).toArray().join(''));
            jQuery('.actions .message').text(jQuery('table.results tr.selected').size() + " rows are selected");
        }
    });
    jQuery('.selectors a.selectPage').click(function() {
        jQuery('table.results tbody tr').removeClass('selected');
        jQuery('table.results tbody td:first-child').click();
        return false;
    });
    jQuery('.selectors a.selectAll').click(function() {
        jQuery('.selectors a.selectPage').click();
        BGB.search.bulk.actions.removeAllIdentifiers();
        jQuery('div.actions form').append('<input type="hidden" name="query" value="' + jQuery('meta[name="query"]').attr('content') + '">');
        jQuery('.actions .message').text('ALL ' + BGB.search.rowCount() +' rows are selected');b

        return false;
    });
});