<!DOCTYPE html>
<html>
<head>
    <title>Batch Operations</title>
</head>
<body>
$message()$
<div class="actions">
    $get(name="import")$
    $get(name="export")$
    $post(name="delete")$
    $post(name="backup", content={
        $input(name="location", placeholder="location", message="")$
    })$
</div>
</body>
</html>