<tr>
    <td><a href="$it.uri$">$it.uri$</a></td>
    <td><div class="entity">$it.reason$</div></td>
    <td>
        $post(name="retry", value="Retry", url=retryUrl, id=it.id)$
        $post(name="ignore", value="Ignore", url=ignoreUrl, id=it.id)$
    </td>
</tr>