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
        $input(name="query", value=query)$
        <input type="submit" value="Search" class="search"/>
    </form>
    $if(advanced)$
        $post(url={delete?query=$query$&amp;advanced=$advanced$}, name="delete")$
    $endif$
    $results()$
</div>
</body>
</html>