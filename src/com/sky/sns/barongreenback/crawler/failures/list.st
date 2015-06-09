<!DOCTYPE html>
<html>
<head>
    <title>Crawler Failures</title>
    <meta name="query" content="$query$"/>
    <meta name="queryException" content="$queryException$"/>
</head>
<body class="failures">
$if(queryException)$
<form class="search" method="get" action="">
    $input(name="query", value=query, message="Invalid Query", messageType="error")$
    <input type="submit" value="Search" class="search"/>
</form>
$else$

$message()$
<form class="search" method="get" action="">
    $input(name="query", value=query, message="")$
    <input type="submit" value="Search" class="search"/>
</form>
<div id="results">
    <table class="$if(pager.paged)$paged$endif$ shouldExpand results">
        <thead>
        <tr>
            $sorter/th(headers=headers, sortedHeaders=sortedHeaders)$
            $th(class="orange", header="Actions")$
        </tr>
        </thead>
        <tbody>
        $if(items)$
        $items:item(item=it)$
        $endif$
        </tbody>
    </table>

    <div class="pagination-controls">
        $pager$
    </div>
    <div class="clear"></div>
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
</div>
$endif$
</body>
</html>