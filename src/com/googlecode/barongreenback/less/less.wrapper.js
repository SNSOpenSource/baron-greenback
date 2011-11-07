function parseLess(content, loader) {
    this.readFile = function(path) {
        return String(loader.call(path.href));
    };
    var parser = new less.Parser;

    var result;
    parser.parse(String(content), function (e, css) {
        if (e) {
            result =  e;
        } else {
            result = css.toCSS();
        }
    });

    return result;
}