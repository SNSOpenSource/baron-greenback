<!DOCTYPE html>
<html>
<head>
    <title>Crawler</title>
    <meta name="decorator" content="none"/>
</head>
<body>

<ul class="tabs">
    $views:{ view | <li class="view $view.name$ $if(view.current)$ active $endif$">
        <a href="/$view.url$">$view.name$</a>
    </li>}$
</ul>

</body>
</html>