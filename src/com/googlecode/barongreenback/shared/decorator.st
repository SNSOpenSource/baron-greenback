<!DOCTYPE html>
<html>
<head>
    <title>$title$ - Baron Greenback</title>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1"/>
    <link rel="shortcut icon" href="$base$baron-greenback/shared/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="$base$baron-greenback/shared/style.less"/>
    $head$
</head>
<body class="$body.class$ bgb">

<div class="topbar">
    <div class="fill">
        <div class="container">
            <h3><a href="/" class="home">Baron Greenback: $title$</a></h3>
            <ul class="nav secondary-nav">
                <li class="dropdown">
                    <ul class="dropdown-menu">
                        $bgbAdmin()$
                    </ul>
                    <a href="#" class="dropdown-toggle">Admin</a>
                </li>
            </ul>
        </div>
    </div>
</div>

<div class="content">
    $include.({$base$views/menu?current=$meta.view$&query=$meta.query$}).body$
    $body$
</div>
<script type="text/javascript" src="$base$baron-greenback/shared/jquery-1.6.4.js"></script>
<script type="text/javascript" src="$base$baron-greenback/shared/jquery.tablesorter.min.js"></script>
<script type="text/javascript" src="$base$baron-greenback/shared/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="$base$baron-greenback/shared/bgb.js"></script>
<script type="text/javascript" src="$base$baron-greenback/shared/subfeed.js"></script>
<script type="text/javascript" src="$base$baron-greenback/shared/expander/expander.js"></script>
</body>
</html>