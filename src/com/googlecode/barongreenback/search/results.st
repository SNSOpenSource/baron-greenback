$if(!results.empty)$
<div id="results">
    <table class="results zebra-striped">
        <thead>
        <tr>
            $headers:th(class="blue " + it.escapedName, header=it.name),th(class="green " + it.escapedName, header=it.name),th(class="orange " + it.escapedName, header=it.name),th(class="purple " + it.escapedName, header=it.name)$
        </tr>
        </thead>
        <tbody>
        $results:{ record |
            <tr>$headers:{ header | <td class="$header.escapedName$">$if(header.unique)$<a href='unique?query=$header.name$:$record.(header.name)$'>$endif$$record.(header.name)$$if(header.unique)$</a>$endif$</td>}$ </tr>
        }$
        </tbody>
    </table>
</div>
$endif$