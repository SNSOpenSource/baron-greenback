

<fieldset class="fields $prefix$">
    <legend>$title$</legend>
    $input(name={$prefix$recordName}, label="Root XPath", value=definition.recordName)$
    <ol>
        $definition.fields:li(content={$keywordDefinition(id=i, name=prefix, keyword=it)$})$
    </ol>
</fieldset>