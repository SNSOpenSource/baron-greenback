<!DOCTYPE html>
<html>
<head>
    <title>$view$ Search</title>
    <meta name="view" content="$view$" />
    <meta name="query" content="$query$" />
    <meta name="drills" content="$drills$" />
    <meta name="resultCount" content="$resultCount$" />
    <meta name="queryException" content="$queryException$" />
</head>
<body class="$view$">
<div id="search-results">
    <form class="search" method="get" action="shortcut">
        $if(queryException)$
            $input(name="query", value=query, message=queryException, messageType="error")$
        $else$
            $input(name="query", value=query)$
        $endif$
        <input type="submit" value="Search" class="search"/>
    </form>
    $results()$
</div>

<script src="$base$baron-greenback/shared/actions/actions.js" type="text/javascript" defer="defer"></script>
</body>
</html>