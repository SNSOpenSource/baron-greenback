<!DOCTYPE html>
<html>
<head>
    <title>Actions</title>
    <meta name="decorator" content="none"/>
</head>
<body>
<div class="actions">
    <a href="#" class="selectPage">Select Page</a>
    $actions: {action |
        <form action="$action.url$" method="$action.method$" class="$action.name$"><input type="submit" class="$action.name$" value="$action.name$"/></form>
    }$
    <div class="message"></div>
</div>
</body>
</html>