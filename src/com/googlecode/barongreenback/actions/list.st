<!DOCTYPE html>
<html>
<head>
    <title>Actions</title>
    <meta name="decorator" content="none"/>
</head>
<body>
<div class="actions">
    <a href="#" class="selectPage">Select Page</a>
    <a href="#" class="selectAll">Select ALL</a>
    $actions: {url |
        $include.({$url$}).body;format="raw"$
    }$
    <div class="message"></div>
</div>
</body>
</html>