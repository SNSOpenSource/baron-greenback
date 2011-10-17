<!DOCTYPE html>
<html>
<head>
    <title>$action; null="Edit"$ View</title>
</head>
<body>

<form class="view form-stacked" action="" method="post">
    $input(name="view.name", label="Name", class="name", value=view.name, placeholder="Name")$
    $recordDefinition(title="Fields", prefix="view.", record=view)$
    <div class="actions">
        <input type="submit" value="Save" name="action" class="save"/>
    </div>

</form>

</body>
</html>