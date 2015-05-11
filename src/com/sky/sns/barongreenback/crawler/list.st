<!DOCTYPE html>
<html>
<head>
    <title>Crawlers</title>
</head>
<body class="list">

$message()$
<table class="results">
    $thead(headers=["name", "updates", "from", "action" ])$
    <tbody>
    $items:item(item=it)$
    </tbody>
</table>

<div class="actions">
    <a href="new" class="new">New</a>
    <a href="import" class="import">Import</a>

    $if(items)$
        $post(name="crawlAll", value="Crawl All")$
        $post(name="resetAll", value="Reset All")$
        $post(name="deleteAll", value="Delete All")$
    $else$
        $post(name="crawlAll", value="Crawl All", disabled="true")$
        $post(name="resetAll", value="Reset All", disabled="true")$
        $post(name="deleteAll", value="Delete All", disabled="true")$
    $endif$

    $get(url={$base$crawler/status}, class="crawler-status", name="Status")$
    $get(url={$base$crawler/failures/list}, class="crawler-failures", name="Failures")$
</div>

</body>
</html>