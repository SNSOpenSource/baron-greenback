<!DOCTYPE html>
<html>
<head>
    <title>home</title>
</head>
<body class="$view$">
<div>
    <form class="search" method="get" action="search">
        <input class="search text" type="text" id="query" name="query" value="$query$"/><input type="submit" value="Search"/>

    </form>

    $results()$
</div>
</body>
</html>