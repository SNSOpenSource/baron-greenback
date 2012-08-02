<!DOCTYPE html>
<html>
<head>
    <title>$view$ Search</title>
    <meta name="view" content="$view$" />
    <meta name="query" content="$query$" />
    <meta name="queryException" content="$queryException$" />
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

$include.({/export?query=$query;format="url"$&view=$view;format="url"$}).body;format="raw"$

</body>
</html>