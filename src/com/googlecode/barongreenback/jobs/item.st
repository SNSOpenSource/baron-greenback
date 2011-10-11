<tr>
    <td><span class="id">$item.id$</span></td>
    <td><span class="seconds">$item.seconds$</span></td>
    <td><span class="request">$item.request$</span></td>
    <td><span class="response">$item.response$</span></td>
    <td>
        $get(name="edit", id=item.id)$
        $post(name="delete", id=item.id)$
    </td>
</tr>