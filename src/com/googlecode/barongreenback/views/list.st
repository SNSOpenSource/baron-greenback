<!DOCTYPE html>
<html>
<head>
    <title>Views</title>
</head>
<body class="list views">

<table class="results">
    $thead(headers=["Name", "action" ])$
    <tbody>
    $views:item(item=it)$
    </tbody>
</table>

<div class="actions">
    <a href="new" class="new">New</a>
    <a href="import" class="import">Import</a>
    $if(views)$
        $post(name="deleteAll", value="Delete All")$
    $else$
        $post(name="deleteAll", value="Delete All", disabled="true")$
    $endif$
</div>

</body>
</html>