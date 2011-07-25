<table class="results">
    <thead>
    <tr>
        $headers:{<th>$it$</th>}$
    </tr>
    </thead>
    <tbody>
    $results:{ record |
        <tr>$headers:{ header | <td>$record.(header)$</td> }$ </tr>
    }$
    </tbody>
</table>