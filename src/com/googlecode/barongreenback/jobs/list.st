<!DOCTYPE html>
<html>
<head>
    <title>Jobs</title>
</head>
<body class="list">

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="method")$
        $th(class="green", header="uri")$
        $th(class="orange", header="interval")$
    </tr>
    </thead>
    <tbody>
    $jobs:item(item=it)$
    </tbody>
</table>

<!--<div class="actions">-->
    <!--<a href="new" class="new">New</a>-->
    <!--<a href="import" class="import">Import</a>-->
<!--</div>-->

</body>
</html>
