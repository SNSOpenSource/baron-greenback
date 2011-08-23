$(document).ready(function() {
    $("input.subfeed").live('click', function() {
        var keywordDefinition = new KeywordDefinition($(this).closest("div.keywordDefinition"), "div.recordDefinitionTemplate", "input.subfeedPrefix");

        if (this.checked) {
            keywordDefinition.showOrAddSubfeed();
        } else {
            keywordDefinition.hideSubfeed();
        }
    });
    $("input.more").live("click", function() {
        var ol = $("ol", $(this).parent());
        var template = $("li.keywordTemplate", ol);
        var nextIndex = ol.children().length;
        var html = "<li>" + template.html().replace(/REPLACE_ME/g, nextIndex) + "</li>";
        var newKeyword = $(html).hide().insertBefore(template);
        newKeyword.show("slow").css({display:'list-item'});
    })
});

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
        template = template.replace(/REPLACE_ME/g, subfeedPrefix);

        $("div.subrecordDefinition", this.content).hide().html(template).show("slow");
    }

    this.hideSubfeed = function() {
        $("div.subrecordDefinition", this.content).hide('slow');
    }

    this.showSubfeed = function() {
        $("div.subrecordDefinition", this.content).show('slow');
    }
    
    this.showOrAddSubfeed = function() {
        this.hasSubfeed() ? this.showSubfeed() : this.addSubfeed();
    }
}