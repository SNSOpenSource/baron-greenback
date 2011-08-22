$(document).ready(function() {
    $("input.subfeed").live('click', function() {
        var keywordDefinition = new KeywordDefinition($(this).parent("div.keywordDefinition"), "div.recordDefinitionTemplate", ".fields");

        if (this.checked) {
            keywordDefinition.showOrAddSubfeed();
        } else {
            keywordDefinition.hideSubfeed();
        }
    });
});

function KeywordDefinition(keywordDefinition, subfeedTemplateSelector, subfeedSelector) {
    this.content = keywordDefinition;
    this.template = $([subfeedTemplateSelector, subfeedSelector].join(" "));
    this.subfeedSelector = subfeedSelector;

    this.hasSubfeed = function() {
        return this.content.find(this.subfeedSelector).length > 0;
    }

    this.addSubfeed = function(subfeed) {
        subfeed.appendTo(this.content);
    }

    this.hideSubfeed = function() {
        $(this.subfeedSelector, this.content).hide();
    }

    this.showSubfeed = function() {
        $(this.subfeedSelector, this.content).show();
    }
    
    this.showOrAddSubfeed = function() {
        this.hasSubfeed() ? this.showSubfeed() : this.addSubfeed(this.template.clone());
    }
}