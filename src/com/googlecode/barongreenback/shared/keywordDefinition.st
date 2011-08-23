<div class="keywordDefinition">
    $input(id=prefix + "keywordName" + id, name=prefix + "keywordName", class="keywordName", label="XPath", value=keyword.name)$

    $input(id=prefix + "alias" + id, name=prefix + "aliases", class="alias", label="Alias", value=keyword.alias)$

    <label for="prefix + type$id$">Type:</label>
    <select id="prefix + type$id$" name="$prefix$types">
    <option value="java.lang.String" selected="selected">String</option>
    <option value="java.lang.Integer">Integer</option>
    <option value="java.lang.Long">Long</option>
    <option value="java.util.Date">Date</option>
    <option value="java.net.URI">URI</option>
    </select>

    $checkbox(id=prefix + "unique" + id, name=prefix + "unique", class="unique", label="Unique", checked=keyword.unique)$
    $checkbox(id=prefix + "visible" + id, name=prefix + "visible", class="visible", label="Visible", checked=keyword.visible)$
    $checkbox(id=prefix + "subfeed" + id, name=prefix + "subfeed", class="subfeed", label="Subfeed", checked=keyword.subfeed)$
    $input(id=prefix + "subfeedPrefix" + id, name=prefix + "subfeedPrefix", class="subfeedPrefix", value=prefix + "field" + id, type="hidden")$

    <div class="subrecordDefinition">
    $if(keyword.definition)$
        $recordDefinition(title="SubFeed " + id, prefix="subfeed" + id, record=keyword.record)$
    $endif$
    </div>
</div>