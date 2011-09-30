<!DOCTYPE html>
<html>
<head>
    <title>Jobs</title>
</head>
<body class="list">

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="crawler")$
        $th(class="green", header="interval (seconds)")$
        $th(class="orange", header="action")$
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
