$(document).ready(function() {

    speed = "normal";

    $("input.subfeed").live('click', function() {
        var keywordDefinition = new KeywordDefinition($(this).closest("div.keywordDefinition"), "div.recordDefinitionTemplate", "input.subfeedPrefix");

        if (this.checked) {
            keywordDefinition.showOrAddSubfeed();
        } else {
            keywordDefinition.hideSubfeed();
        }
    });
    $("input.more").live("click", function() {
        var ol = $(this).parent().children("ol")[0];
        var template = $(ol).children("li.keywordTemplate")[0];
        var nextIndex = $(ol).children().length;
        var html = "<li>" + $(template).html().replace(/KEYWORD_ID_REPLACE_ME/g, nextIndex) + "</li>";
        var newKeyword = $(html).hide().insertBefore(template);
        newKeyword.show().css({display:'list-item'});
    });
    $("table.results").tablesorter({ sortList: [
        [0,0]
    ] });

    $("form > fieldset > ol.fields").sortable({
        placeholder: "placeholder",
        items: '> li',
        deactivate: function(event, ui) {
            $(ui.item).parent().children().each(function (index) {
                $(this).find("label").each(function() {
                    fixAttribute(this, "for", index)
                });
                $(this).find("input, select, textarea, button").each(function() {
                    fixAttribute(this, "id", index)
                    fixAttribute(this, "name", index)
                });
            });
        }
    });

    $(".closeIcon").live("click", function() {
        $(this).parent().parent().remove();
    });

});

function fixAttribute(element, name, index) {
    var attr = $(element).attr(name);
    if(typeof(attr) == "undefined"){
        return
    }
    var newValue = attr.replace(new RegExp("(^form\\.record\\.keywords\\[)(\\d+)"), "$1" + (index + 1));
    $(element).attr(name, newValue);
}


function KeywordDefinition(keywordDefinition, subfeedTemplateSelector, subfeedPrefixSelector) {
    this.content = keywordDefinition;
    this.template = $(subfeedTemplateSelector);
    this.subfeedPrefixSelector = subfeedPrefixSelector;

    this.hasSubfeed = function() {
        return $("div.subrecordDefinition", this.content).children().length > 0;
    }

    this.addSubfeed = function() {
        var template = this.template.html();
        var subfeedPrefix = $(this.subfeedPrefixSelector, this.content).attr("value");
        template = template.replace(/RECORD_PREFIX_REPLACE_ME/g, subfeedPrefix);

        $("div.subrecordDefinition", this.content).hide().html(template).show(speed);
    }

    this.hideSubfeed = function() {
        $("div.subrecordDefinition", this.content).hide(speed);
    }

    this.showSubfeed = function() {
        $("div.subrecordDefinition", this.content).show(speed);
    }

    this.showOrAddSubfeed = function() {
        this.hasSubfeed() ? this.showSubfeed() : this.addSubfeed();
    }
}