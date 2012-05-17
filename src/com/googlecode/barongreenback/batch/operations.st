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
<script>
    //<![CDATA[
    if (window.location.hostname != 'localhost') {
        window.onload = function() {
            jQuery('input[class="delete"]').click(function() {
                var a = Math.floor(Math.random()*11) + 2;
                var b = Math.floor(Math.random()*11) + 2;
                return a == prompt('You are currently running on ' + window.location.hostname + '. Please confirm you want to DELETE EVERYTHING by solving this equation:\n' + (a*b) + ' / ' + b + ' = ?');
            });
        };
    }
    //]]>
</script>
</body>
</html>