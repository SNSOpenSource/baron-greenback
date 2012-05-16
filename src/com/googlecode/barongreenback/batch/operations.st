<!DOCTYPE html>
<html>
<head>
    <title>Batch Operations</title>
</head>
<body>
$message()$

    $group(name="Backup", content={
        $post(name="backup", content={
            $input(name="location", value=backupLocation, message="")$
        })$
    })$

    $group(name="Restore", content={
        $post(name="restore", content={
            $input(name="location", value=restoreLocation, message="")$
        })$
    })$

<div class="actions">
    $get(name="import")$
    $get(name="export")$
    $post(name="delete")$
</div>
</body>
</html>