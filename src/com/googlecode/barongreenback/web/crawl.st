<!DOCTYPE html>
<html>
<head>
    <title>Crawler</title>
</head>
<body>

<form class="crawl" action="/admin/crawl" method="POST">

    <div>
        <label for="update">Update:</label><input id="update" name="update" value="" class="text recordName" type="text"/>
    </div>

    <div>
        <label for="from">From:</label><input id="from" name="from" value="" class="text url" type="text"/>
    </div>

    $fields(title="Fields", name="")$

    $fields(title="SubFeed1 Fields", name="subfeed1")$

    <input type="submit" value="Crawl"/>
</form>


</body>
</html>