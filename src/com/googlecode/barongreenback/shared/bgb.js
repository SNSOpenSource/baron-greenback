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