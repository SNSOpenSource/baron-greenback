<table>
    $failures: { failure |
    <tr>
        <td>$failure.id$</td>
        <td>
            <form action="/crawler/failures/retry" method="get">
                <input type="hidden" name="id" value="$failure.id$"/>
                <input type="submit" value="retry" class="btn"/>
            </form>
        </td>
    </tr>
    }$
</table>