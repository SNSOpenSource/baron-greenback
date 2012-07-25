$if(results)$
<div id="results">
    <table class="results $if(pager.paged)$paged$endif$ zebra-striped">
        <thead>
        <tr>
            $sorter/th(headers=headers, sortedHeaders=sortedHeaders)$
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