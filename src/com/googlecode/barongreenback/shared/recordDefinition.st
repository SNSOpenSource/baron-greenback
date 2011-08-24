<fieldset class="fields $prefix$">
    <legend>$title$</legend>
    $input(name={$prefix$recordName}, label="Root XPath", value=record.name, placeholder="/xpath/")$
    <ol>
        $record.keywords:li(content={$keywordDefinition(id=i, prefix=prefix, keyword=it)$})$
        $if(!record.keywords)$
            $li(content={$keywordDefinition(id="0", prefix=prefix)$}, keyword=emptyKeyword)$
        $endif$
        $li(content={$keywordDefinition(id="KEYWORD_ID_REPLACE_ME", prefix=prefix)$}, class="keywordTemplate", keyword=emptyKeyword)$
    </ol>
    <input type="button" value="More" class="more"/>
</fieldset>