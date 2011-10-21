<!DOCTYPE html>
<html>
<head>
    <title>Import Crawler</title>
</head>
<body>

<form class="import form-stacked" action="import" method="POST">
    $group(name="Import", content={
        $textarea(name="model", label="Model", help="Accepts crawling definition in json format")$
        $input(name="id", label="Id", help="Optional")$
    })$
    <div class="actions">
        <input type="submit" name="action" value="Import" class="import"/>
    </div>
</form>

</body>
</html>