<!DOCTYPE html>
<html>
<head>
    <title>Menu</title>
    <meta name="decorator" content="none"/>
</head>
<body>

<ul class="tabs">
    $views:{ view |
    <li class="view $view.name$ count$view.itemsTotal$ $if(view.current)$ active $endif$">
        <a class="tab" href="$view.url$">$view.name$ <span class="count">($view.itemsTotal$)</span></a>
    </li>
    }$
</ul>

</body>
</html>