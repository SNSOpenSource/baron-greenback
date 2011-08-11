<!DOCTYPE html>
<html>
<head>
    <title>Crawler</title>
    <meta name="decorator" content="none">
</head>
<body>

<ul class="views">
    $views:{ view | <li class="view $view.name$ $if(view.current)$ current $endif$">
        <a href="/$view.url$">$view.name$</a>
    </li>}$
</ul>

</body>
</html>