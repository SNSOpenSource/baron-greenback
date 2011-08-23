

<fieldset class="fields $prefix$">
    <legend>$title$</legend>
    $input(name={$prefix$name}, label="Root XPath", value=record.name)$
    <ol>
        $record.keywords:li(content={$keywordDefinition(id=i, name=prefix, keyword=it)$})$
    </ol>
</fieldset>