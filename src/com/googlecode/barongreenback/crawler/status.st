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

<ul>
    <li><a href="$base$crawler/failures/list">Failures</a></li>
</ul>

</body>
</html>