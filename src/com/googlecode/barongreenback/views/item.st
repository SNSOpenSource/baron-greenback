<tr>
    <td><a href="edit?id=$item.name$" class="update">$item.name$</a></td>
    <td>
        $get(name="edit", id=item.name)$
        $post(name="delete", id=item.name)$
    </td>
</tr>
