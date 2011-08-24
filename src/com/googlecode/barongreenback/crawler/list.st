<!DOCTYPE html>
<html>
<head>
    <title>Crawlers</title>
</head>
<body>

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
            <td><a href="edit?id=$item.id$">$item.model.form.update$</a></td>
            <td><a href="$item.model.form.from$">$item.model.form.from$</a></td>
            <td><a href="export?id=$item.id$" class="export">Export</a>
            <form action="delete" class="delete" method="post">
                <input type="hidden" name="id" value="$item.id$"/>
                <input type="submit" value="Delete" class="delete"/>
            </form>
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