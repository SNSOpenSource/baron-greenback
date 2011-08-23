

<fieldset class="fields $prefix$">
    <legend>$title$</legend>
    $input(name={$prefix$recordName}, label="Root XPath", value=record.name, placeholder="/xpath/")$
    <ol>
        $record.keywords:li(content={$keywordDefinition(id=i, prefix=prefix, keyword=it)$})$
        $li(content={$keywordDefinition(id="REPLACE_ME", prefix=prefix)$}, class="keywordTemplate")$
    </ol>
    <input type="button" value="More" class="more"/>
</fieldset>