<!DOCTYPE html>
<html>
<head>
    <title>$action; null="Edit"$ Crawler</title>
</head>
<body>

<form class="crawl form-stacked" action="" method="POST">

    $input(name="form.update", label="Update", class="update", value=form.update, placeholder="Name")$
    $input(name="form.from", label="From", class="from", value=form.from, placeholder="http://www.example.com/feed/xml")$
    $input(name="form.more", label="More", class="from", value=form.more, placeholder="//link[@rel='prev-archive']/@href")$

    $recordDefinition(title="Fields", prefix="form.record.", record=form.record)$

    <div class="actions">
        <input type="submit" value="Save" name="action" class="save"/>
        <a href="list" class="cancel">Cancel</a>
    </div>
</form>

<div class="recordDefinitionTemplate">
    $recordDefinition(title="Fields", prefix="RECORD_PREFIX_REPLACE_ME")$
</div>
</body>
</html>