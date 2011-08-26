<!DOCTYPE html>
<html>
<head>
    <title>$action; null="Edit"$ View</title>
</head>
<body>

<form class="view form-stacked" action="" method="post">
    $recordDefinition(title="Fields", prefix="", record=form.record)$

    <div class="actions">
        <input type="submit" value="Save" name="action" class="save"/>
    </div>

</form>

</body>
</html>