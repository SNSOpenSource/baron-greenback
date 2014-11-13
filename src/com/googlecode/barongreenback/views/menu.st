<!DOCTYPE html>
<html>
<head>
    <title>Menu</title>
    <meta name="decorator" content="none"/>
</head>
<body>
<div class="views-menu">
<div id="nav-views-container">
    <h4>Show results in</h4>
<ul class="nav nav-views">
    $views:{ view |
    <li class="view $view.name$ count$view.itemsTotal$ $if(view.current)$ active $endif$">
        <a class="tab" href="$view.url$">$view.name$ <span class="count">($view.itemsTotal$)</span></a>
    </li>
    }$
</ul>
</div>
</div>
</body>
</html>