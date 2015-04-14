<tr class="completed">
    <td><span class="jobstatus $item.status$">$item.status$</span></td>
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
        $if(item.response)$
            <span class="code">$item.response.code$</span> <span class="status">$item.response.status$</span>
            <div class="entity">$item.response.entity$</div>
<!--
$item.response.raw$
-->
        $endif$
        </div>
    </td>
    <td><span class="started">$item.started$</span></td>
    <td><span class="completed">$item.completed$</span></td>
    <td><span class="duration">$item.duration$</span></td>
</tr>