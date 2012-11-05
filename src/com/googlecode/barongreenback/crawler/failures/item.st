<tr>
    <td><a href="$it.uri$">$it.uri$</a></td>
    <td><div class="entity">$it.reason$</div></td>
    <td>
        $post(name="retry", value="Retry", id=it.id)$
        $post(name="delete", value="Delete", id=it.id)$
    </td>
</tr>