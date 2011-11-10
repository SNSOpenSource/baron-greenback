<tr>
    <td><a href="edit?id=$item.id$" class="update">$item.model.form.update$</a></td>
    <td><a href="$item.model.form.from$" class="from">$item.model.form.from$</a></td>
    <td>
        $post(url=item.jobUrl, name="crawl", id=item.id)$
        $get(name="edit", id=item.id)$
        $get(name="export", id=item.id)$
        $if(item.resettable)$
            $post(name="reset", id=item.id)$
        $else$
            $post(name="reset", id=item.id, disabled="true")$
        $endif$
        $post(name="delete", id=item.id)$
    </td>
</tr>