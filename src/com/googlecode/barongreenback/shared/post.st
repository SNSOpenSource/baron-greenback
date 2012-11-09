<form action="$url; null=name$" class="$name$" method="POST">
    $content; null=""$
    $if(noid)$$else$$if(id)$<input type="hidden" name="id" value="$id$"/>$endif$$endif$
    <input type="submit" value="$value; null=name$" class="$name$" $if(disabled)$ disabled="disabled" $endif$/>
</form>