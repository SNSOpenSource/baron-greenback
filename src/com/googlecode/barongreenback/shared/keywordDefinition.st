<div class="keywordDefinition">
    $input(id=prefix + "name", name=prefix + "name", class="keywordName", label="Name", value=keyword.name)$

    $input(id=prefix + "alias", name=prefix + "alias", class="alias", label="Alias", value=keyword.alias)$
    $input(id=prefix + "group", name=prefix + "group", class="group", label="Group", value=keyword.group)$

    <div class="control">
        <label for="$prefix$type">Type:</label>
        <select id="$prefix$type" name="$prefix$type">
            $types:{<option value="$it.value$" $if(it.(keyword.type))$ selected="selected" $endif$>$it.name$</option>}$
        </select>
    </div>

    $checkbox(id=prefix + "unique", name=prefix + "unique", class="unique", label="Unique", checked=keyword.unique)$
    $checkbox(id=prefix + "visible", name=prefix + "visible", class="visible", label="Visible", checked=keyword.visible)$
    $checkbox(id=prefix + "subfeed", name=prefix + "subfeed", class="subfeed", label="Subfeed", checked=keyword.subfeed)$
    $input(id=prefix + "subfeedPrefix", name=prefix + "subfeedPrefix", class="subfeedPrefix", value=prefix + "record.", type="hidden")$

    <div class="subrecordDefinition">
    $if(keyword.record)$
        $recordDefinition(title="Fields", prefix=prefix + "record.", record=keyword.record)$
    $endif$
    </div>
</div>