<!DOCTYPE html>
<html>
<head>
    <title>$view$ Search</title>
    <meta name="view" content="$view$" />
    <meta name="query" content="$query$" />
</head>
<body class="$view$">
<div>
    <form class="search" method="get">
        $if(queryException)$
            $input(name="query", value=query, message="Invalid Query", messageType="error")$
            <!-- $queryException$ -->
        $else$
            $input(name="query", value=query)$
        $endif$
        <input type="submit" value="Search" class="search"/>
    </form>
    $if(advanced)$
        $post(url={delete?query=$query$&amp;advanced=$advanced$}, name="delete")$
    $endif$
    $results()$
</div>
</body>
</html>