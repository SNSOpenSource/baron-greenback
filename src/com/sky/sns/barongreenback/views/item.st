<tr>
    <td><a href="edit?id=$item.id$" class="name">$item.name$</a></td>
    <td>
        $get(name="edit", id=item.id)$
        $get(name="export", id=item.id)$
        $post(name="copy", id=item.id)$
        $post(name="delete", id=item.id)$
    </td>
</tr>
