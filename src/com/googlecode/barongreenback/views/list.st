<!DOCTYPE html>
<html>
<head>
    <title>Views</title>
</head>
<body class="list views">

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="Name")$
        $th(class="orange", header="action")$
    </tr>
    </thead>
    <tbody>
    $views:item(item=it)$
    </tbody>
</table>

<div class="actions">
    <a href="new" class="new">New</a>
    <a href="import" class="import">Import</a>
</div>

</body>
</html>