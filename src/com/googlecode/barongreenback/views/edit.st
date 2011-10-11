<!DOCTYPE html>
<html>
<head>
    <title>$action; null="Edit"$ View</title>
</head>
<body>

<form class="view form-stacked" action="" method="post">
    $input(name="form.update", label="Update", class="update", value=form.update, placeholder="Name")$
    $recordDefinition(title="Fields", prefix="form.record.", record=form.record)$
    <div class="actions">
        <input type="submit" value="Save" name="action" class="save"/>
    </div>

</form>

</body>
</html>