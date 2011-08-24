<!DOCTYPE html>
<html>
<head>
    <title>Crawlers</title>
</head>
<body>


<form class="import form-stacked" action="import" method="post">
    <fieldset>
        <legend>Import</legend>
        <div class="control">
            <label for="model">Model:</label>
            <textarea class="model" id="model" name="model"></textarea>
            <span class="help-block">Accepts crawling definition in json format</span>
        </div>
    </fieldset>
    <div class="actions">
        <input type="submit" value="Import" class="import"/>
    </div>
</form>

</body>
</html>