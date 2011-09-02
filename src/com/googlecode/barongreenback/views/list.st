<!DOCTYPE html>
<html>
<head>
    <title>Views</title>
</head>
<body class="list">

<table class="results zebra-striped">
    <thead>
    <tr>
        $th(class="blue", header="Name")$
        $th(class="orange", header="action")$
    </tr>
    </thead>
    <tbody>
    $views:{ item |
    <tr>
        <td><a href="edit?id=$item.name$">$item.name$</a></td>
        <td>
            $get(name="edit", id=item.name)$
            $post(name="delete", id=item.name)$
        </td>
    </tr>
    }$
    </tbody>
</table>

<div class="actions">
    <a href="new" class="new">New</a>
</div>

</body>
</html>