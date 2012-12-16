<!DOCTYPE html>
<html>
<head>
    <title>Active Crawler</title>
    <meta name="decorator" content="none"/>
</head>
<body>
<form method="POST" action="change" class="activeCrawler">
    $select(name="crawler", label="Active Crawler", value=activeCrawler, values=crawlers)$
    <input type="submit" value="change"/>
</form>
</body>
</html>