<h5 class="facet-name">$facet.name$</h5>
<ul class="facet $facet.("class-name")$">
    $facet.entries:{ entry |
        <li class="facet-entry"><label><input type="checkbox" value="$entry.value$" class="facet-entry-checkbox" $if(entry.drilledDown)$checked="checked"$endif$ />
            <span class="facet-entry-name $if(entry.drilledDown)$drilled-down$endif$" title="$entry.displayValue$">$entry.displayValue$</span><span class="facet-entry-count">$entry.itemsTotal$</span></label>
        </li>
    }$
    $if(facet.more)$
        <li class="facet-show-more"><a href="$facet.more;format="html"$&amp;decorator=none" title="Show More" class="facet-show-more-link">Show More...</a></li>
    $endif$
    $if(facet.fewer)$
    <li class="facet-show-fewer"><a href='$facet.fewer;format="html"$&amp;decorator=none' title="Show Fewer" class="facet-show-fewer-link">Show Fewer...</a></li>
    $endif$
</ul>
