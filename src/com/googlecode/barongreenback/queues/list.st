<!DOCTYPE html>
<html>
<head>
    <title>Queues</title>
    <meta http-equiv="refresh" content="30"/>
</head>
<body class="list">

<table class="results zebra-striped">
    $thead(headers=["Status", "Request", "Response", "Started", "Completed", "Duration (Seconds)" ])$
    <tbody>
    $running:item(item=it)$
    $completed:item(item=it)$
    </tbody>
</table>

</body>
</html>
