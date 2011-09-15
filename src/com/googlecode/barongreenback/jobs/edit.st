<!DOCTYPE html>
<html>
<head>
    <title>$action; null="Edit"$ Scheduled Job</title>
</head>
<body>

<form class="job form-stacked" action="reschedule" method="POST">

    $input(name="id", type="hidden", class="id", value=id)$
    $input(name="interval", label="Interval (seconds)", class="interval", value=interval)$

    <div class="actions">
        <input type="submit" value="Save" name="action" class="save"/>
        <a href="list" class="cancel">Cancel</a>
    </div>
</form>

</body>
</html>