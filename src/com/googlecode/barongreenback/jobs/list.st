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
        $th(class="purple", header="action")$
    </tr>
    </thead>
    <tbody>
    $jobs:item(item=it)$
    </tbody>
</table>

</body>
</html>
