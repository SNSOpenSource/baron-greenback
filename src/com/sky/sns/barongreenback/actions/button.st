<!DOCTYPE html>
<html>
<head>
    <title>Action</title>
    <meta name="decorator" content="none"/>
</head>
<body>
<form action="$url$" method="$method$" class="$name$">
    $if(query)$<input type="hidden" name="query" value="$query$"/>$endif$
    $if(drills)$<input type="hidden" name="drills" value="$drills$"/>$endif$
    <input type="submit" class="$name$" value="$name$"/>
</form>
</body>
</html>