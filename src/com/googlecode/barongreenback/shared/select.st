$control(content={
    <select id="$id; null=name$" name="$name$" class="$class; null=name$">
        $values:{<option value="$it.value$" $if(it.(value))$ selected="selected" $endif$>$it.name$</option>}$
    </select>
})$
