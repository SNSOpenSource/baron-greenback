<!DOCTYPE html>
<html>
<head>
    <title>$view$ Details</title>
    <meta name="view" content="$view$"/>
    <meta name="query" content="$query$"/>
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