$(document).ready(function() {
    $("input.subfeed").live('click', function() {
        var kewordDefinition = $(this).parent("div.keywordDefinition");

        if(this.checked) {
            if(kewordDefinition.find(".fields").length > 0) {
                $(".fields", kewordDefinition).show();
            } else {
                var subfeed = $("div.recordDefinitionTemplate .fields").clone();
                subfeed.appendTo(kewordDefinition);
            }
        } else {
            $(".fields", kewordDefinition).hide();
        }
    });
});