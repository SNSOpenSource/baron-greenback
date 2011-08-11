<!DOCTYPE html>
<html>
<head>
    <title>baron-greenback : $title$</title>
    <link type="text/css" rel="stylesheet" href="$base$default.css"/>
</head>
<body class="$body.class$">
<h1 class="title">baron-greenback : $title$</h1>
<p class="summary">A Lucene based web search tool</p>
<div class="header">
    <ul class="menu">
        <li class="item"><a href="$base$">home</a></li>
        <li class="item"><a href="$base$admin/crawl">admin</a></li>
    </ul>
</div>

$include.({/views/list?current=$body.class$}).body$

<div class="content">
$body$
</div>
<div class="footer">
    copyleft <a href="http://code.google.com/p/baron-greenback/">baron-greenback</a> - 2011
</div>
</body>
</html>