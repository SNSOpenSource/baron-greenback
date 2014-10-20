<!DOCTYPE html>
<html>
<head>
    <title>$view$ Search</title>
    <meta name="view" content="$view$" />
    <meta name="query" content="$query$" />
    <meta name="resultCount" content="$resultCount$" />
    <meta name="queryException" content="$queryException$" />
</head>
<body class="$view$">
<div>
    <form class="search" method="get" action="shortcut">
        $if(queryException)$
            $input(name="query", value=query, message="Invalid Query", messageType="error")$
            <!-- $queryException$ -->
        $else$
            $input(name="query", value=query)$
        $endif$
        <input type="submit" value="Search" class="search"/>
    </form>
    $results()$
</div>

$include.({/$view;format="url"$/actions/list?query=$query;format="url"$$if(advanced)$&advanced$endif$}).body;format="raw"$
<script src="$base$baron-greenback/shared/actions/actions.js" type="text/javascript" defer="defer"></script>
</body>
</html>