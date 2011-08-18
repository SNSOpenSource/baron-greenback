<!DOCTYPE html>
<html>
<head>
    <title>Crawler</title>
</head>
<body>

<form class="crawl" action="" method="POST">

    <div>
        $input(name="update", label="Update")$
    </div>

    <div>
        $input(name="from", label="From")$
    </div>

    $recordDefinition(title="Fields", prefix="", numberOfFields=numberOfFields)$

    $recordDefinition(title="SubFeed1 Fields", prefix="subfeed1", numberOfFields=numberOfFields)$

    <input type="submit" value="Crawl"/>
</form>


</body>
</html>