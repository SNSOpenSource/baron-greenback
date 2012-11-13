<html>
<head>
    <meta http-equiv="refresh" content="1"/>
</head>
<body>
<table>
    $thead(headers=["Name", "Active Threads", "Queued Jobs" ])$
    $executors : {executor |
    <tr>
        <td>$executor.name$</td>
        <td>$executor.activeThreads$</td>
        <td>$executor.size$</td>
    </tr>
    }$
</table>

<div class="actions">
    $get(url={$base$crawler/failures/list}, class="crawler-failures", name="Failures")$
</div>


</body>
</html>