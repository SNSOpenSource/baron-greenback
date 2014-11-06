if (typeof BGB == 'undefined') {
    BGB = {};
}

BGB.namespace = function () {
    var a = arguments, o = null, i, j, d;
    for (i = 0; i < a.length; i = i + 1) {
        d = ("" + a[i]).split(".");
        o = BGB;

        // BGB is implied, so it is ignored if it is included
        for (j = (d[0] == "BGB") ? 1 : 0; j < d.length; j = j + 1) {
            o[d[j]] = o[d[j]] || {};
            o = o[d[j]];
        }
    }

    return o;
};

BGB.namespace('search').rowCount = function () {
    return parseInt(jQuery('meta[name="resultCount"]').attr('content'));
};

BGB.namespace('search').selectedRowCount = function () {
    return BGB.search.allPagesSelected
        ? BGB.search.rowCount()
        : jQuery('.actions >form:first-child input[name="id"]').length;
};

BGB.namespace('search').allPagesSelected = false;

BGB.encodeTextToHtmlEntities = function (text) {
    return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
};