<div id="results">
    <table class="results zebra-striped">
        <thead>
        <tr>
            $headers:th(class="blue", header=it.name),th(class="green", header=it.name),th(class="orange", header=it.name),th(class="purple", header=it.name)$
        </tr>
        </thead>
        <tbody>
        $results:{ record |
            <tr>$headers:{ header | <td class="$header.name$">$if(header.unique)$<a href='unique?query=$header.name$:$record.(header.name)$'>$endif$$record.(header.name)$$if(header.unique)$</a>$endif$</td>}$ </tr>
        }$
        </tbody>
    </table>
</div>