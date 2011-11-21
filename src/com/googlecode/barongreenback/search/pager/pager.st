<div class="pagination">
  <ul>
    $pages : { page |
        <li class="$html(page.cssClass)$ page"><a $if (page.link)$href="$html(page.link)$"$endif$>$page.text$</a></li>
    }$
  </ul>
</div>