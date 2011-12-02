<form action="$url; null=name$" class="$name$" method="POST">
    $if(id)$<input type="hidden" name="id" value="$id$"/>$endif$
    <input type="submit" value="$value; null=name$" class="$name$" $if(disabled)$ disabled="disabled" $endif$/>
</form>