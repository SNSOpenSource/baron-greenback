<!DOCTYPE html>
<html>
<head>
    <title>baron-greenback : $title$</title>
    <link type="text/css" rel="stylesheet" href="$base$reset.css"/>
    <link type="text/css" rel="stylesheet" href="$base$default.css"/>
    <link type="text/css" rel="stylesheet" href="$base$menu.css"/>
</head>
<body class="$body.class$">
<div class="header">
    <ul class="menu">
        <li class="item"><a href="$base$admin/crawl">Admin</a></li>
    </ul>
</div>
<h1 class="title">baron-greenback : $title$</h1>
<p class="summary">A Lucene based web search tool</p>

$include.({/menu?current=$body.class$}).body$

<div class="content">
$body$
</div>
<div class="footer">
    copyleft <a href="http://code.google.com/p/baron-greenback/">baron-greenback</a> - 2011
</div>
</body>
</html>