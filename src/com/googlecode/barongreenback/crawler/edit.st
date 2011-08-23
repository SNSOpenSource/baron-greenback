<!DOCTYPE html>
<html>
<head>
    <title>Crawler</title>
</head>
<body>

<form class="crawl" action="" method="POST">

    <div>
        $input(name="update", label="Update", value=form.update)$
    </div>

    <div>
        $input(name="from", label="From", value=form.from)$
    </div>

    $recordDefinition(title="Fields", prefix="", record=form.record)$

    <input type="submit" value="Crawl" name="action"/>
    <input type="submit" value="Save" name="action"/>
</form>

<div class="recordDefinitionTemplate">
        $recordDefinition(title="Fields", prefix="REPLACE_ME.", record=form.record)$
</div>
</body>
</html>