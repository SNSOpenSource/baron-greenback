<!DOCTYPE html>
<html>
<head>
    <title>$view$ Details</title>
    <meta name="view" content="$view$"/>
    <meta name="query" content="$query$"/>
    <meta name="drills" content=""/>
$record.keys:{ groupName | $record.(groupName).keys:{ name |
    <meta name="record.$name$" content='$record.(groupName).(name); format="raw,html"$'/>
}$}$

</head>
<body>
    <div id="quicklinks">
        <h4 class="title">Sections</h4>
        <ul class="nav nav-quicklinks">
        </ul>
    </div>
    <div id="uniquedata">
        <div class="$view$ unique">
        $record.keys:{ groupName |
            $group(name=groupName, content={
                $record.(groupName).keys:{ name |
                    $nameValue(name=name, value=record.(groupName).(name))$
                }$
            })$
        }$
        </div>
    </div>

</body>
</html>