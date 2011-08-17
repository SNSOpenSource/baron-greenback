<!DOCTYPE html>
<html>
<head>
    <title>home</title>
</head>
<body class="$view$">
<div>
    <form class="search" method="get" action="search">
        $input(name="query", value=query)$
        <input type="submit" value="Search"/>
    </form>

    $search/results()$
</div>
</body>
</html>