<!DOCTYPE html>
<html>
<head>
    <title>Crawler</title>
</head>
<body>

<form class="crawl" action="/admin/crawl" method="POST">

    <label for="url">Url:</label><input id="url" name="url" value="" class="text url" type="text"/>

    <label for="recordName">Record:</label><input id="recordName" name="recordName" value="" class="text recordName" type="text"/>

    <label for="elementXPath">Root XPath:</label><input id="elementXPath" name="elementXPath" value="" class="text elementXPath" type="text"/>

    $fields(title="Fields", name="")$

    $fields(title="SubFeed1 Fields", name="subfeed1")$

    <input type="submit" value="Submit"/>
</form>


</body>
</html>