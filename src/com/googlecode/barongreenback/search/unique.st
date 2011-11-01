<!DOCTYPE html>
<html>
<head>
    <title>$view$ Details</title>
    <meta name="view" content="$view$"/>
    <meta name="query" content="$query$"/>
$record.keys:{ groupName | $record.(groupName).keys:{ name |
    <meta name="record.$name$" content='$record.(groupName).(name); format="raw,html"$'/>
}$}$

</head>
<body class="$view$">
    $record.keys:{ groupName |
        $group(name=groupName, content={
            $record.(groupName).keys:{ name |
                $nameValue(name=name, value=record.(groupName).(name))$
            }$
        })$
    }$
</body>
</html>