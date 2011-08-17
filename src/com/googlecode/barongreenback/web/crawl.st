<!DOCTYPE html>
<html>
<head>
    <title>Crawler</title>
</head>
<body>

<form class="crawl" action="/admin/crawl" method="POST">

    <div>
        $text(id="update", name="update", class="text update", label="Update")$
    </div>

    <div>
        $text(id="from", name="from", class="text from", label="From")$
    </div>

    $fields(title="Fields", prefix="")$

    $fields(title="SubFeed1 Fields", prefix="subfeed1")$

    <input type="submit" value="Crawl"/>
</form>


</body>
</html>