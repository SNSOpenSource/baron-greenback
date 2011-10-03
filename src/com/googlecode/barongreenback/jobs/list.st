<!DOCTYPE html>
<html>
<head>
    <title>Jobs</title>
</head>
<body class="list">

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="Id")$
        $th(class="green", header="Interval (seconds)")$
        $th(class="orange", header="Request")$
        $th(class="blue", header="Response")$
        $th(class="green", header="action")$
    </tr>
    </thead>
    <tbody>
    $jobs:item(item=it)$
    </tbody>
</table>

<div class="actions">
    $post(name="start")$
    $post(name="stop")$
</div>

</body>
</html>
