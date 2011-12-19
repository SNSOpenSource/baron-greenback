<!DOCTYPE html>
<html>
<head>
    <title>Batch Import</title>
</head>
<body>

$if(message)$
    <div class="alert-message $message.category$">
        <span class="message">$message.text$</span>
    </div>
$endif$
<form class="import form-stacked" action="import" method="POST">
    $group(name="Batch Import", content={
        $textarea(name="model", value=model, label="Models", message="Accepts batch crawling/view definitions in json format e.g. UUID1:model1, UUID2:model2")$
    })$
    <div class="actions">
        <input type="submit" name="action" value="Import" class="import"/>
    </div>
</form>
</body>
</html>