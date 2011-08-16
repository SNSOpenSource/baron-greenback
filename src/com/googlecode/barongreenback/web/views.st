<!DOCTYPE html>
<html>
<head>
    <title>Views</title>
</head>
<body>

<ul>
    $views:{ view | <li>
        <a href="/$view.url$">$view.name$</a>
    </li>}$
</ul>

</body>
</html>