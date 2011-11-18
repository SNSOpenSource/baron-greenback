$if(results)$
<div id="results">
    <table class="results zebra-striped">
        <thead>
        <tr>
            $headers:th(class="blue " + it.escapedName, header=it.name),th(class="green " + it.escapedName, header=it.name),th(class="orange " + it.escapedName, header=it.name),th(class="purple " + it.escapedName, header=it.name)$
        </tr>
        </thead>
        <tbody>
        $results:{ record |
            <tr>$headers:{ header | <td class="$header.escapedName$">$if(record.(header.name).url)$<a href='$record.(header.name).url$'>$endif$$record.(header.name).value$$if(record.(header.name).url)$</a>$endif$</td>}$ </tr>
        }$
        </tbody>
    </table>
</div>
$endif$