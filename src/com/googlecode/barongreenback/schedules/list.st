<!DOCTYPE html>
<html>
<head>
    <title>Schedules</title>
    <meta http-equiv="refresh" content="30"/>
</head>
<body class="list">

<table class="results zebra-striped">
    $thead(headers=["Status", "Start Time", "Interval (Seconds)", "Request", "Response", "Started", "Completed", "Duration (Seconds)", "action" ])$
    <tbody>
    $schedules:item(item=it)$
    </tbody>
</table>

<div class="actions">
    $post(name="start", value="start scheduler")$
    $post(name="stop", value="stop scheduler")$
    $if(anyExists)$
        $post(name="deleteAll", value="delete all")$
    $else$
        $post(class="deleteAllDanger", name="deleteAll", value="Delete All", disabled=true)$
    $endif$
</div>

</body>
</html>
