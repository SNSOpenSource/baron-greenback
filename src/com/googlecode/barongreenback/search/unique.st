<!DOCTYPE html>
<html>
<head>
    <title>home</title>
</head>
<body class="$view$">
<div>

<dl>
    $record.keys:{ key |
        <dt>$key$</dt>
        <dd>$record.(key)$</dd>
    }$
</dl>

</div>
</body>
</html>