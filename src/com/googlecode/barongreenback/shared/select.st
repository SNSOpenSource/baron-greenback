<div class="control $class; null=name$">
    $if(label)$<label for="$id; null=name$">$label$:</label>$endif$
    <select id="$id; null=name$" name="$name$" class="$class; null=name$">
        $values:{<option value="$it.value$" $if(it.(value))$ selected="selected" $endif$>$it.name$</option>}$
    </select>
</div>
