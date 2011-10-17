<!DOCTYPE html>
<html>
<head>
    <title>Menu</title>
    <meta name="decorator" content="none"/>
</head>
<body>

<ul class="tabs">
    $views:{ view |
    <li class="view $view.name$ $if(view.current)$ active $endif$">
        <a class="tab" href="$view.url$">$view.name$</a>
    </li>
    }$
</ul>

</body>
</html>