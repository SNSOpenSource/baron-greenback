<!DOCTYPE html>
<html>
<head>
    <title>Crawler Failures</title>
</head>
<body class="failures">

$message()$
<table class="$if(pager.paged)$paged$endif$ shouldExpand results">
    <thead>
    <tr>
        $sorter/th(headers=headers, sortedHeaders=sortedHeaders)$
        $th(class="orange", header="Actions")$
    </tr>
    </thead>
    <tbody>
    $items:item(item=it)$
    </tbody>
</table>

<div class="pagination-controls">
    $pager$
</div>

<div class="actions">
    $get(url={$base$crawler/status}, class="crawler-status", name="Status")$
    $if(items)$
        $post(name="retryAll", value="Retry All")$
        $post(name="deleteAll", value="Delete All")$
    $else$
        $post(name="retryAll", value="Retry All", disabled="true")$
        $post(name="deleteAll", value="Delete All", disabled="true")$
    $endif$
</div>

</body>
</html>