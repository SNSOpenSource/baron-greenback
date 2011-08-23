<!DOCTYPE html>
<html>
<head>
    <title>baron-greenback : $title$</title>
    <link type="text/css" rel="stylesheet" href="$base$shared/reset.css"/>
    <link rel="stylesheet/less" type="text/css" href="$base$shared/default.less"/>
    <link rel="stylesheet/less" type="text/css" href="$base$views/menu.less"/>
    <script src="$base$shared/less-1.1.3.min.js" type="text/javascript"></script>
</head>
<body class="$body.class$">
<div class="header">
    <ul class="menu">
        <li class="item"><a href="$base$crawler/new?numberOfFields=10">Admin</a></li>
    </ul>
</div>
<h1 class="title">baron-greenback : $title$</h1>
<p class="summary">A Lucene based web search tool</p>

$include.({$base$views/menu?current=$body.class$}).body$

<div class="content">
$body$
</div>
<div class="footer">
    copyleft <a href="http://code.google.com/p/baron-greenback/">baron-greenback</a> - 2011
</div>
<script type="text/javascript" src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript" src="$base$shared/subfeed.js"></script>
</body>
</html>