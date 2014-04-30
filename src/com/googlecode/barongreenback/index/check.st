<!DOCTYPE html>
<html>
<head>
    <title>Crawlers</title>
</head>
<body class="list">

$message()$
<table class="results">
    $thead(headers=["name", "status", "action" ])$
    <tbody>
    $items:item(item=it)$
    </tbody>
</table>

<div class="actions">
    $if(items.empty)$
        $post(name="fixAll", value="Fix All", disabled="true")$
    $else$
        $post(name="fixAll", value="Fix All")$
    $endif$
</div>

</body>
</html>