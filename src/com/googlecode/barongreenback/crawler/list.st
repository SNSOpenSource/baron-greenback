<!DOCTYPE html>
<html>
<head>
    <title>Crawlers</title>
</head>
<body>

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="updates")$ $th(class="green", header="from")$
    </tr>
    </thead>
    <tbody>
    $items:{ item |
        <tr><td><a href="edit?id=$item.id$">$item.model.form.update$</a></td><td><a href="$item.model.form.from$">$item.model.form.from$</a></td></tr>
    }$
    </tbody>
</table>

<div class="actions">
    <a href="new" class="new">New</a>
</div>

</body>
</html>