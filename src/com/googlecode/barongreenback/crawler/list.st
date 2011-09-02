<!DOCTYPE html>
<html>
<head>
    <title>Crawlers</title>
</head>
<body class="list">

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="updates")$
        $th(class="green", header="from")$
        $th(class="orange", header="action")$
    </tr>
    </thead>
    <tbody>
    $items:{ item |
    <tr>
        <td><a href="edit?id=$item.id$" class="update">$item.model.form.update$</a></td>
        <td><a href="$item.model.form.from$" class="from">$item.model.form.from$</a></td>
        <td>
            $post(name="crawl", id=item.id)$
            $get(name="edit", id=item.id)$
            $get(name="export", id=item.id)$
            $post(name="delete", id=item.id)$
        </td>
    </tr>
    }$
    </tbody>
</table>

<div class="actions">
    <a href="new" class="new">New</a>
    <a href="import" class="import">Import</a>
</div>

</body>
</html>