<tr>
    <td><a href="edit?id=$item.id$" class="update">$item.model.form.update$</a></td>
    <td><a href="$item.model.form.from$" class="from">$item.model.form.from$</a></td>
    <td>
        $post(url={$base$jobs/schedule/30/crawler/crawl}, name="crawl", id=item.id)$
        $get(name="edit", id=item.id)$
        $get(name="export", id=item.id)$
        $post(name="delete", id=item.id)$
    </td>
</tr>