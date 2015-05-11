<div class="fieldset fields $prefix$">
    <div class="legend">$title$</div>
    $input(id={$prefix$name}, name={$prefix$name}, class="rootXPath", label="Root XPath", value=record.name, placeholder="/xpath/")$
    <ol class="fields">
        $record.keywords:li(content={$keywordDefinition(id=i, prefix=prefix + "keywords[" + i + "].", keyword=it)$})$
        $if(!record.keywords)$
            $li(content={$keywordDefinition(id="1", prefix=prefix + "keywords[1].")$}, keyword=emptyKeyword)$
        $endif$
        $li(content={$keywordDefinition(id="KEYWORD_ID_REPLACE_ME", prefix=prefix + "keywords[KEYWORD_ID_REPLACE_ME].")$}, class="keywordTemplate", keyword=emptyKeyword)$
    </ol>
    <input type="button" value="More" class="more"/>
</div>