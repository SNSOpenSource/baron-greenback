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
        $th(class="blue", header="Request")$
        $th(class="green", header="Response")$
        $th(class="orange", header="Started")$
        $th(class="blue", header="Completed")$
        $th(class="green", header="Duration (Seconds)")$
    </tr>
    </thead>
    <tbody>
    $completed:item(item=it)$
    </tbody>
</table>

</body>
</html>
