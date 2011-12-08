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
    $post(url=item.jobUrl, name="crawlAll", value="Crawl All", id=item.id)$
    $post(name="resetAll", value="Reset All", id=item.id)$
    $post(name="deleteAll", value="Delete All", id=item.id)$
</div>

</body>
</html>