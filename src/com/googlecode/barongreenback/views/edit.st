<!DOCTYPE html>
<html>
<head>
    <title>$action; null="Edit"$ View</title>
</head>
<body>
<form class="view form-stacked" action="" method="post">
    $input(name="view.name", label="Name", class="name", value=view.name, placeholder="Name")$
    $input(name="view.query", label="Query", class="query", value=view.query, placeholder="+type:your_type")$
    <fieldset class="fields view">
        <legend>Fields</legend>
        <ol class="fields">
            $view.keywords:li(content={$fieldDefinition(id=i, prefix="view.keywords[" + i + "].", keyword=it)$})$
            $if(!view.keywords)$
                $li(content={$fieldDefinition(id="1", prefix="view.keywords[1].")$}, keyword=emptyKeyword)$
            $endif$
            $li(content={$fieldDefinition(id="KEYWORD_ID_REPLACE_ME", prefix="view.keywords[KEYWORD_ID_REPLACE_ME].")$}, class="keywordTemplate", keyword=emptyKeyword)$
        </ol>
        <input type="button" value="More" class="more"/>
    </fieldset>
    <div class="actions">
        <input type="submit" value="Save" name="action" class="save"/>
    </div>
</form>
</body>
</html>