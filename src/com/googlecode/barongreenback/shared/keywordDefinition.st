<div class="keywordDefinition">
    $input(id=name + "field" + id, name=name + "fields", class="field", label="XPath", value=keyword.name)$

    $input(id=name + "alias" + id, name=name + "aliases", class="alias", label="Alias", value=keyword.alias)$

    <label for="name + type$id$">Type:</label>
    <select id="name + type$id$" name="$name$types">
    <option value="java.lang.String" selected="selected">String</option>
    <option value="java.lang.Integer">Integer</option>
    <option value="java.lang.Long">Long</option>
    <option value="java.util.Date">Date</option>
    <option value="java.net.URI">URI</option>
    </select>

    $checkbox(id=name + "unique" + id, name=name + "unique", class="unique", label="Unique", checked=keyword.unique)$
    $checkbox(id=name + "visible" + id, name=name + "visible", class="visible", label="Visible", checked=keyword.visible)$
    $checkbox(id=name + "subfeed" + id, name=name + "subfeed", class="subfeed", label="Subfeed", checked=keyword.subfeed)$
    $input(id=name + "subfeedPrefix" + id, name=name + "subfeedPrefix", class="subfeedPrefix", value=name + "field" + id, type="hidden")$

    <div class="subrecordDefinition">
    $if(keyword.definition)$
        $recordDefinition(title="SubFeed " + id, prefix="subfeed" + id, definition=keyword.definition)$
    $endif$
    </div>
</div>




