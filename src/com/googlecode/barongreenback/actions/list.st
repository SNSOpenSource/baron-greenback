<!DOCTYPE html>
<html>
<head>
    <title>Actions</title>
    <meta name="decorator" content="none"/>
</head>
<body>
<div class="actions">
    $actions: {url |
        $include.({$url$}).body;format="raw"$
    }$
</div>
</body>
</html>