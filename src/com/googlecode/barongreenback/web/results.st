<table class="results">
    <thead>
    <tr>
        $headers:{<th>$it.name$</th>}$
    </tr>
    </thead>
    <tbody>
    $results:{ record |
        <tr>$headers:{ header | <td>$if(header.unique)$<a href='unique?query=$header.name$:"$record.(header.name)$"'>$endif$ $record.(header.name)$ $if(header.unique)$</a>$endif$  </td> }$ </tr>
    }$
    </tbody>
</table>