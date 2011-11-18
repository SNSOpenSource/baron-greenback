<!DOCTYPE html>
<html>
<head>
    <title>Crawlers</title>
</head>
<body class="list">

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="updates")$
        $th(class="green", header="from")$
        $th(class="orange", header="action")$
    </tr>
    </thead>
    <tbody>
    $items:item(item=it)$
    </tbody>
</table>

<div class="actions">
    <a href="new" class="new">New</a>
    <a href="import" class="import">Import</a>
</div>

</body>
</html>