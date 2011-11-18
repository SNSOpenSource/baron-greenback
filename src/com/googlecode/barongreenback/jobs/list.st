<!DOCTYPE html>
<html>
<head>
    <title>Jobs</title>
    <meta http-equiv="refresh" content="30"/>
</head>
<body class="list">

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="Status")$
        $th(class="green", header="Interval (Seconds)")$
        $th(class="orange", header="Request")$
        $th(class="blue", header="Response")$
        $th(class="green", header="Started")$
        $th(class="orange", header="Completed")$
        $th(class="blue", header="Duration (Seconds)")$
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
