<!DOCTYPE html>
<html>
<head>
    <title>baron-greenback : $title$</title>
    <link type="text/css" rel="stylesheet" href="$base$shared/reset.css"/>
    <link type="text/css" rel="stylesheet" href="$base$shared/default.css"/>
    <link type="text/css" rel="stylesheet" href="$base$views/menu.css"/>
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
<script type="text/javascript" src="http://code.jquery.com/jquery-latest.min.js"/>
<script type="text/javascript" src="$base$shared/subfeed.js"/>
</body>
</html>