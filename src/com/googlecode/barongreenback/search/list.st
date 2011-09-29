<!DOCTYPE html>
<html>
<head>
    <title>$view$ Search</title>
    <meta name="view" content="$view$" />
</head>
<body class="$view$">
<div>
    <form class="search" method="get">
        $input(name="query", value=query)$
        <input type="submit" value="Search" class="search"/>
    </form>

    $results()$
</div>
</body>
</html>