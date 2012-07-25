<!DOCTYPE html>
<html>
<head>
    <title>Crawler Failures</title>
</head>
<body class="failures">

$message()$
<table class="$if(pager.paged)$paged$endif$ shouldExpand results zebra-striped">
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
$pager$

<div class="actions">
    $if(anyExists)$
        $post(name="retryAll", value="Retry All", url=retryAll)$
        $post(name="ignoreAll", value="Ignore All", url=ignoreAll)$
    $else$
        $post(name="retryAll", value="Retry All", disabled="true")$
        $post(name="ignoreAll", value="Ignore All", disabled="true")$
    $endif$
</div>

</body>
</html>