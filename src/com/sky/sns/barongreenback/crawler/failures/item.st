<tr>
    <td><div class="entity">$it.requestTime$</div></td>
    <td><a href="$it.uri$">$it.uri$</a></td>
    <td><div class="entity">$it.crawlerId$</div></td>
    <td><div class="entity jobType">$it.jobType$</div></td>
    <td><div class="entity reason">$it.reason$</div></td>
    <td><div class="entity">$it.duration$</div></td>
    <td>
        <div class="Actions">
        $post(url=retryUrl, name="retry", value="Retry", id=it.id)$
        $post(url=deleteUrl, name="delete", value="Delete", id=it.id)$
        </div>
    </td>
</tr>