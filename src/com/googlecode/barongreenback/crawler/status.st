<html>
<head>
    <meta http-equiv="refresh" content="1"/>
</head>
<body>
<table>
    <tr>
        <th>Name</th>
        <th>Active Threads</th>
        <th>Queued Jobs</th>
    </tr>
    $executors : {executor |
    <tr>
        <td>$executor.name$</td>
        <td>$executor.activeThreads$</td>
        <td>$executor.size$</td>
    </tr>
    }$
</table>
</body>
</html>