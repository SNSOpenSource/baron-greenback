$text(id="field" + id, name=name + "fields", class="field", label="XPath")$

$text(id="alias" + id, name=name + "aliases", class="alias", label="Alias")$

<label for="type$id$">Type:</label>
<select id="type$id$" name="$name$types">
<option value="java.lang.String" selected="selected">String</option>
<option value="java.lang.Integer">Integer</option>
<option value="java.lang.Long">Long</option>
<option value="java.util.Date">Date</option>
<option value="java.net.URI">URI</option>
<option value="java.net.URI#subfeed1">SubFeed1</option>
</select>

$checkbox(id="unique" + id, name=name + "unique", class="unique", label="Unique")$

