<!DOCTYPE html>
<html>
<head>
    <title>baron-greenback : $title$</title>
    <link rel="stylesheet/less" type="text/css" href="$base$shared/twitter/twitter.less"/>
    <script src="$base$shared/less-1.1.3.min.js" type="text/javascript"></script>
</head>
<body class="$body.class$">

<div class="topbar">
      <div class="fill">
        <div class="container">
          <h3><a href="/">Baron Greenback: $title$</a></h3>
          <ul class="nav secondary-nav">
            <li class="menu">
                <li><a href="$base$crawler/list">Admin</a></li>
            </li>
          </ul>
        </div>
      </div> <!-- /fill -->
    </div>

<div class="content">
$include.({$base$views/menu?current=$meta.view$}).body$
$body$
</div>
<div class="footer">
    copyleft <a href="http://code.google.com/p/baron-greenback/">baron-greenback</a> - 2011
</div>
<script type="text/javascript" src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript" src="$base$shared/jquery.tablesorter.min.js"></script>
<script type="text/javascript" src="$base$shared/subfeed.js"></script>
</body>
</html>