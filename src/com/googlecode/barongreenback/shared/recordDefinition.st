

<fieldset class="fields $prefix$">
    <legend>$title$</legend>
    $input(name={$prefix$recordName}, label="Root XPath", value=record.name)$
    <ol>
        $record.keywords:li(content={$keywordDefinition(id=i, prefix=prefix, keyword=it)$})$
    </ol>
</fieldset>