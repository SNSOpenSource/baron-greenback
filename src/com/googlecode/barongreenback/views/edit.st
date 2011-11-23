<!DOCTYPE html>
<html>
<head>
    <title>$action; null="Edit"$ View</title>
</head>
<body>
<form class="view form-stacked" action="" method="post">
    $input(name="view.name", label="Name", class="name", value=view.name, placeholder="Name")$
    $input(name="view.records", label="Records", class="records", value=view.records, placeholder="Records")$
    $input(name="view.query", label="Query", class="query", value=view.query, placeholder="name:value")$
    $checkbox(id="view.visible", name="view.visible", class="visible", label="Visible", checked=view.visible)$
    $input(name="view.priority", label="Priority", class="priority", value=view.priority, placeholder="10")$
    <div class="fieldset fields view">
        <div class="legend">Fields</div>
        <ol class="fields">
            $view.keywords:li(content={$fieldDefinition(id=i, prefix="view.keywords[" + i + "].", keyword=it)$})$
            $if(!view.keywords)$
                $li(content={$fieldDefinition(id="1", prefix="view.keywords[1].")$}, keyword=emptyKeyword)$
            $endif$
            $li(content={$fieldDefinition(id="KEYWORD_ID_REPLACE_ME", prefix="view.keywords[KEYWORD_ID_REPLACE_ME].")$}, class="keywordTemplate", keyword=emptyKeyword)$
        </ol>
        <input type="button" value="More" class="more"/>
    </div>
    <div class="actions">
        <input type="submit" value="Save" name="action" class="save"/>
    </div>
</form>
</body>
</html>