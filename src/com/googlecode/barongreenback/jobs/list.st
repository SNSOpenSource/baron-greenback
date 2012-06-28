<!DOCTYPE html>
<html>
<head>
    <title>Jobs</title>
    <meta http-equiv="refresh" content="30"/>
</head>
<body class="list">

<table class="results zebra-striped">
    $thead(headers=["Status", "Interval (Seconds)", "Request", "Response", "Started", "Completed", "Duration (Seconds)", "action" ])$
    <tbody>
    $jobs:item(item=it)$
    </tbody>
</table>

<div class="actions">
    $post(name="start")$
    $post(name="stop")$
    $if(anyExists)$
        $post(name="deleteAll", value="Delete All")$
    $else$
        $post(name="deleteAll", value="Delete All", disabled="true")$
    $endif$
</div>

</body>
</html>
