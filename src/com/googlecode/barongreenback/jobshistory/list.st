<!DOCTYPE html>
<html>
<head>
    <title>Jobs History</title>
    <meta name="query" content="$query$" />
    <meta name="queryException" content="$queryException$" />
</head>
<body class="list jobsHistory">
<form class="search" method="get" action="">
    $if(queryException)$
        $input(name="query", value=query, message="Invalid Query", messageType="error")$
        <!-- $queryException$ -->
    $else$
        $input(name="query", value=query)$
    $endif$
    <input type="submit" value="Search" class="search"/>
</form>
<div id="results">
<table class="results $if(pager.paged)$paged$endif$ table table-striped">
    <thead>
        <tr>
            $sorter/th(headers=headers, sortedHeaders=sortedHeaders)$
        </tr>
    </thead>
    <tbody>
$items:item(item=it)$
    </tbody>
</table>
<div class="pagination-controls">
    $pager$
</div>
<div class="clear"></div>
</div>

</body>
</html>
