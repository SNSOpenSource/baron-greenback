<div class="pagination">
  <ul>
    $pages : { page |
        <li class="$html(page.cssClass)$ page"><a $if (page.link)$href="$html(page.link)$"$endif$>$page.text$</a></li>
    }$
  </ul>
  <form method="get">
        $pager.queryParametersToUrl : { param |
            <input type="hidden" name="$param.key$" value="$param.value$"/>
        } $
        $select(id="pageOptions", name="page.rows", class="rowsPerPage", label="Show", value=pageOptions.currentRowsPerPage, values=pageOptions.rowsPerPage)$
    <div class="control"><input type="submit" value="Show"/></div>
  </form>
</div>