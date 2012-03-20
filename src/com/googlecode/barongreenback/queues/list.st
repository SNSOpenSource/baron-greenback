<!DOCTYPE html>
<html>
<head>
    <title>Queues</title>
    <meta http-equiv="refresh" content="30"/>
</head>
<body class="list">

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="Status")$
        $th(class="green", header="Request")$
        $th(class="orange", header="Response")$
        $th(class="blue", header="Started")$
        $th(class="green", header="Completed")$
        $th(class="orange", header="Duration (Seconds)")$
    </tr>
    </thead>
    <tbody>
    $running:item(item=it)$
    $completed:item(item=it)$
    </tbody>
</table>

</body>
</html>
