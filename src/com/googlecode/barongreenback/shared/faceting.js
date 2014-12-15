BGB.namespace('faceting').registerFacetEntriesHandlers = function() {

    var body = jQuery('body');
    var mainContent = $('.side-menu').next();
    var originalSearchResultsPadding = parseInt(mainContent.css('padding-left'));

    function toggleSideMenu(mainContentSize) {
        BGB.tables.hideFixedHeader();
        $('.views-menu-container').first().toggle();
        mainContent.css('padding-left', mainContentSize);
//        $('#search-results').css('padding-left', mainContentSize);
        BGB.tables.redrawFixedHeader();
    }

    body.on('click', '.side-menu-toggle-close', function () {
        toggleSideMenu('40px');
        $(this).removeClass('side-menu-toggle-close').addClass('side-menu-toggle-open');
    });

    body.on('click', '.side-menu-toggle-open', function () {
        toggleSideMenu(originalSearchResultsPadding + 'px');
        $(this).removeClass('side-menu-toggle-open').addClass('side-menu-toggle-close');
    });

    body.on('click', 'a.facet-show-more-link', function (e) {
        rebuildFacet(e);
    });

    body.on('click', 'a.facet-show-fewer-link', function (e) {
        rebuildFacet(e);
    });

    body.on('click', '.facet-entry-checkbox', function(e) {
        e.preventDefault();
        var facets = jQuery('meta[name=drills]').attr('content');

        var drillParam  = BGB.namespace('faceting').generateDrillParameter($(this), facets);
        reloadWithDrills(drillParam);
    });

    body.on('click', '.remove-all-filters-link', function (e) {
        e.preventDefault();
        reloadWithDrills('{}');
    });

    function reloadWithDrills(drillDowns) {
        var currentPageHref = window.location.href;
        if (currentPageHref.indexOf("drills=") > -1) {
            window.location = currentPageHref.replace(/drills=.*&?/, 'drills=' + encodeURIComponent(drillDowns));
        } else {
            var separator = window.location.search ? '&' : '?';
            window.location = currentPageHref + separator + 'drills=' + encodeURIComponent(drillDowns);
        }
    }

    function rebuildFacet(e){
        e.preventDefault();
        var me = $(e.target);
        var url = me.attr('href');
        jQuery.get(url).done(function (data) {
            me.parents('li.nav-facet').html(data);
        });
    }

};

BGB.namespace('faceting').generateDrillParameter = function(selectedFacetEntry, facets){
    var parsedFacets = JSON.parse(facets ? facets : '{}');

    var facetName = selectedFacetEntry.parents('.nav-facet').find('.facet-name').text();
    var selectedFacetEntries = parsedFacets[facetName] === undefined ? [] : parsedFacets[facetName];
    var entryValue = selectedFacetEntry.val();
    var indexOfEntry = selectedFacetEntries.indexOf(entryValue);
    if (indexOfEntry > -1) {
        selectedFacetEntries.splice(indexOfEntry, 1);
    } else {
        selectedFacetEntries.push(entryValue);
    }
    if (selectedFacetEntries.length == 0) {
        delete parsedFacets[facetName];
    } else {
        parsedFacets[facetName] = selectedFacetEntries;
    }
    return JSON.stringify(parsedFacets);
};

jQuery(document).ready(function () {
    BGB.faceting.registerFacetEntriesHandlers();
});

