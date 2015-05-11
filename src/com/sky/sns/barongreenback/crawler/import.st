<!DOCTYPE html>
<html>
<head>
    <title>Import Crawler</title>
</head>
<body>

<form class="import form-stacked" action="import" method="POST">
    $group(name="Import", content={
        $textarea(name="model", label="Model", message="Accepts crawling definition in json format")$
        $input(name="id", label="Id", message="Optional")$
    })$
    <div class="actions">
        <input type="submit" name="action" value="Import" class="import"/>
    </div>
</form>

</body>
</html>