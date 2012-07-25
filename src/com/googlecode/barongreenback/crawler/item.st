<tr class="crawler">
    <td class="name">$item.model.form.name$</td>
    <td class="update">$item.model.form.update$</td>
    <td><a href="$item.model.form.from$" class="from">$item.model.form.from$</a></td>
    <td>
        $post(url=item.jobUrl, name="crawl", id=item.id)$
        $if(item.model.form.disabled)$
            $post(name="enable", id=item.id)$
        $else$
            $post(name="disable", id=item.id)$
        $endif$
        $post(name="copy", id=item.id)$
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