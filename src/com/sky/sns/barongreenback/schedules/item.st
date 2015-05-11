<tr class="job">
    <td><span class="jobstatus $item.status$">$item.status$</span></td>
    <td><span class="start">$item.start$</span></td>
    <td><span class="seconds">$item.seconds$</span></td>
    <td>
        <div class="request">
            <span class="method">$item.request.method$</span> <span class="uri">$item.request.uri$</span>
            <div class="entity">$item.request.entity$</div>
<!--
$item.request.raw$
-->
        </div>
    </td>
    <td>
        <div class="response">
            $if(item.isOk)$
            <span class="code">$item.response.code$</span> <span class="status">$item.response.status$</span>
            $else$
            <span class="status">OK</span>
            $endif$
<!--
$item.response.raw$
-->
        </div>
    </td>
    <td><span class="started">$item.started$</span></td>
    <td><span class="completed">$item.completed$</span></td>
    <td><span class="duration">$item.duration$</span></td>
    <td>
        $get(name="edit", id=item.id)$
        $post(name="delete", id=item.id)$
    </td>
</tr>