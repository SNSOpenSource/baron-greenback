<!DOCTYPE html>
<html>
<head>
    <title>Batch Operations</title>
</head>
<body>
$message()$

<table class="results">
    $thead(headers=["Name", "Size", "Date", "Action" ])$
    <tbody>
    $backups:items(item=it)$
    </tbody>
    <tfoot>
    <tr class="backup">
        <td colspan="4">
            $post(name="restore", content={
                $input(name="location", message="")$
            })$
        </td>
    </tr>
    </tfoot>
</table>

<div class="actions">
    $post(name="backup", content={
        $input(name="location", value=backupLocation, message="")$
    })$
    $get(name="import")$
    $get(name="export")$
    $post(name="deleteAll", value="Delete All")$
    $post(name="scheduleBackup", value="Schedule Backup", url={$base$schedules/schedule/$id$/$start$/$interval$/jobs/run/batch/autobackup}, noid="true")$
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