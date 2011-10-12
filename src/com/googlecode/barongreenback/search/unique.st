<!DOCTYPE html>
<html>
<head>
    <title>$view$ Details</title>
    <meta name="view" content="$view$"/>
    <meta name="query" content="$query$"/>
</head>
<body class="$view$">
<div>
    $record.keys:{ groupName |
    <fieldset class="$groupName$">
        <legend>$groupName$</legend>

        $record.(groupName).keys:{ name |

        <div class="control">
            <span class="label">$name$:</span>
            <span>$record.(groupName).(name)$</span>
        </div>
        }$
    </fieldset>
    }$
</div>
</body>
</html>