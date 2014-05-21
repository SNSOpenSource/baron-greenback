<!DOCTYPE html>
<html>
<head>
    <title>Jobs History</title>
</head>
<body class="list jobsHistory bgb">
<form class="search" method="get" action="">
    $input(name="query", value=query)$
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
</div>
<div class="pagination-controls">
    $pager$
</div>

</body>
</html>
