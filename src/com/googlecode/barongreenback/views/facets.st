<!DOCTYPE html>
<html>
<head>
    <title>Menu</title>
    <meta name="decorator" content="none"/>
    $if(drillDownsException)$
    <meta name="drillDownsException" content="$drillDownsException$"/>
    $endif$
</head>
<body>
$if(drillDownsException)$
<div class="error">Could not understand your refine-by criteria; showing all results</div>
$endif$
$if(facets)$
<div id="nav-facets-container">
<h4>Filter by$if(hasFilters)$ <span class="remove-all-filters">(<a href="#" class="remove-all-filters-link" title="Remove all filters">Remove all filters</a>)</span>$endif$</h4>
    <ul class="nav nav-facets">
    $facets:{ facet |
        <li class="nav-facet">$facet()$</li>
    }$
</ul>
</div>
$endif$
</body>
</html>
