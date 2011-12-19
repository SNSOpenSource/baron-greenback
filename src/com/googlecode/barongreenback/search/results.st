$if(results)$
<div id="results">
    <table class="results $if(pager.paged)$paged$else$unpaged$endif$ zebra-striped">
        <thead>
        <tr>
            $headers:th(class="blue " + it.escapedName + " " + sortedHeaders.(it.name), header=it.name, link=sortLinks.(it.name)),th(class="green " + it.escapedName + " " + sortedHeaders.(it.name), header=it.name, link=sortLinks.(it.name)),th(class="orange " + it.escapedName + " " + sortedHeaders.(it.name), header=it.name, link=sortLinks.(it.name)),th(class="purple " + it.escapedName + " " + sortedHeaders.(it.name), header=it.name, link=sortLinks.(it.name))$
        </tr>
        </thead>
        <tbody>
        $results:{ record |
            <tr>$headers:{ header | <td class="$header.escapedName$">$if(record.(header.name).url)$<a href='$record.(header.name).url$'>$endif$$record.(header.name).value$$if(record.(header.name).url)$</a>$endif$</td>}$ </tr>
        }$
        </tbody>
    </table>
</div>
$pager$
$endif$