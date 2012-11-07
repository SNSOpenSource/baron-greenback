<tr class="backup">
    <td><span class="name">$item.name$</span></td>
    <td><span class="size">$item.size$</span></td>
    <td><span class="date">$item.date$</span></td>
    <td>
        $post(name="restore", content={
            $input(type="hidden", name="location", value=item.location, message="")$
        })$
        $get(name="download", id=item.name)$
        $post(name="delete", id=item.name)$
    </td>
</tr>