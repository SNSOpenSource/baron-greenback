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

    $if(anyExists)$
        $post(url=item.jobUrl, name="crawlAll", value="Crawl All")$
        $post(name="resetAll", value="Reset All")$
        $post(name="deleteAll", value="Delete All")$
    $else$
        $post(url=item.jobUrl, name="crawlAll", value="Crawl All", disabled="true")$
        $post(name="resetAll", value="Reset All", disabled="true")$
        $post(name="deleteAll", value="Delete All", disabled="true")$
    $endif$
</div>

</body>
</html>