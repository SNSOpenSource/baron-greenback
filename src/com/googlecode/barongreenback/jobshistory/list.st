<!DOCTYPE html>
<html>
<head>
    <title>Jobs History</title>
</head>
<body class="list jobsHistory bgb">

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
</div>
<div class="pagination-controls">
    $pager$
</div>

</body>
</html>
