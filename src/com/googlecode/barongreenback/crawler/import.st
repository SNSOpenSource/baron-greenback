<!DOCTYPE html>
<html>
<head>
    <title>Import Crawler</title>
</head>
<body>


<form class="import form-stacked" action="import" method="POST">
    <fieldset>
        <legend>Import</legend>
        $textarea(name="model", label="Model", help="Accepts crawling definition in json format")$
        $input(name="id", label="Id", help="Optional")$
    </fieldset>
    <div class="actions">
        <input type="submit" name="action" value="Import" class="import"/>
    </div>
</form>

</body>
</html>