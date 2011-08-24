<div class="keywordDefinition">
    $input(id=prefix + "keywordName" + id, name=prefix + "keywordName", class="keywordName", label="XPath", value=keyword.name)$

    $input(id=prefix + "alias" + id, name=prefix + "aliases", class="alias", label="Alias", value=keyword.alias)$

    <div class="control">
        <label for="prefix + type$id$">Type:</label>
        <select id="prefix + type$id$" name="$prefix$types">
            $types:{<option value="$it.value$" $if(it.(keyword.type))$ selected="selected" $endif$>$it.name$</option>}$
        </select>
    </div>

    $checkbox(id=prefix + "unique" + id, name=prefix + "unique", class="unique", label="Unique", checked=keyword.unique)$
    $checkbox(id=prefix + "visible" + id, name=prefix + "visible", class="visible", label="Visible", checked=keyword.visible)$
    $checkbox(id=prefix + "subfeed" + id, name=prefix + "subfeed", class="subfeed", label="Subfeed", checked=keyword.subfeed)$
    $input(id=prefix + "subfeedPrefix" + id, name=prefix + "subfeedPrefix", class="subfeedPrefix", value=prefix + "field" + id + ".", type="hidden")$

    <div class="subrecordDefinition">
    $if(keyword.record)$
        $recordDefinition(title="Fields", prefix=prefix + "field" + id + ".", record=keyword.record)$
    $endif$
    </div>
</div>