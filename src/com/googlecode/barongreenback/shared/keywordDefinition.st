<div class="keywordDefinition">
    $input(id=prefix + "name", name=prefix + "name", class="keywordName", label="Name", value=keyword.name)$

    $input(id=prefix + "alias", name=prefix + "alias", class="alias", label="Alias", value=keyword.alias)$
    $input(id=prefix + "group", name=prefix + "group", class="group", label="Group", value=keyword.group)$

    $select(id=prefix + "type", name=prefix + "type", class="type", label="Type", value=keyword.type, values=types)$

    $checkbox(id=prefix + "unique", name=prefix + "unique", class="unique", label="Unique", checked=keyword.unique)$
    $checkbox(id=prefix + "visible", name=prefix + "visible", class="visible", label="Visible", checked=keyword.visible)$
    $checkbox(id=prefix + "subfeed", name=prefix + "subfeed", class="subfeed", label="Subfeed", checked=keyword.subfeed)$
    $checkbox(id=prefix + "checkpoint", name=prefix + "checkpoint", class="checkpoint", label="Checkpoint", checked=keyword.checkpoint)$
    $input(id=prefix + "subfeedPrefix", name=prefix + "subfeedPrefix", class="subfeedPrefix", value=prefix + "record.", type="hidden")$

    <div class="subrecordDefinition">
    $if(keyword.record)$
        $recordDefinition(title="Fields", prefix=prefix + "record.", record=keyword.record)$
    $endif$
    </div>
    <div class="closeIcon"></div>
    <div class="moveIcon"></div>
</div>
