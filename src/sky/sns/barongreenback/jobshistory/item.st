<tr>
    <td>
        $item.timestamp$
    </td>
    <td>
        $item.elapsedTime$
    </td>
    <td class="action">
        $item.action$
    </td>
    <td>
        $item.jobId$
    </td>
    <td>$if(item.shortMessage)$
        <div class="shortMessage">
            $item.shortMessage$
        </div>
        $endif$

        $if(item.rawMessage)$
        <div class="rawMessage">$item.rawMessage$</div>
        $endif$
    </td>
</tr>