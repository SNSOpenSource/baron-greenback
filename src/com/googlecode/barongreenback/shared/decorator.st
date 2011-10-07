<!DOCTYPE html>
<html>
<head>
    <title>$title$ - Baron Greenback</title>
    <link rel="stylesheet/less" type="text/css" href="$base$baron-greenback/shared/style.less"/>
    <script type="text/javascript">var less = less || {env : 'production'};</script>
    <script src="$base$baron-greenback/shared/less-1.1.3.min.js" type="text/javascript"></script>
    $head$
</head>
<body class="$body.class$">

<div class="topbar">
    <div class="fill">
        <div class="container">
            <h3><a href="/" class="home">Baron Greenback: $title$</a></h3>
            <ul class="nav secondary-nav">
                <li class="menu">
                    <a href="$base$views/list">Views Admin</a>
                </li>
                <li class="menu">
                    <a href="$base$crawler/list">Crawler Admin</a>
                </li>
                <li class="menu">
                    <a href="$base$jobs/list">Jobs Admin</a>
                </li>
            </ul>
        </div>
    </div>
</div>

<div class="content">
    $include.({$base$views/menu?current=$meta.view$}).body$
    $body$
</div>
<script type="text/javascript" src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript" src="$base$baron-greenback/shared/jquery.tablesorter.min.js"></script>
<script type="text/javascript" src="$base$baron-greenback/shared/subfeed.js"></script>
</body>
</html>