<div class="pager-and-rowpager">
    <div class="pagination">
      <ul>
        $pages : { page |
            <li class="$html(page.cssClass)$ page"><a $if (page.link)$href="$html(page.link)$"$endif$>$page.text$</a></li>
        }$
      </ul>
    </div>
    <div class="rowpager">
      <form method="get" class="form-inline">
            $pager.queryParametersToUrl : { param |
                <input type="hidden" name="$param.key$" value="$param.value$"/>
            } $
            $select(name="page.rows", class="rowsPerPage", label="Rows", value=pageOptions.currentRowsPerPage, values=pageOptions.rowsPerPage)$
        <div class="control"><input type="submit" value="Show"/></div>
      </form>
  </div>
</div>