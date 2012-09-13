<!DOCTYPE html>
<html>
<head>
    <title>$view$ Search</title>
    <meta name="view" content="$view$" />
    <meta name="query" content="$query$" />
    <meta name="queryException" content="$queryException$" />
</head>
<body class="$view$">
<div>
    <form class="search" method="get">
        $if(queryException)$
            $input(name="query", value=query, message="Invalid Query", messageType="error")$
            <!-- $queryException$ -->
        $else$
            $input(name="query", value=query)$
        $endif$
        <input type="submit" value="Search" class="search"/>
    </form>
    $results()$
</div>

$include.({/$view;format="url"$/actions/list?query=$query;format="url"$$if(advanced)$&advanced$endif$}).body;format="raw"$
<div id="footer">
    <script>
        //<![CDATA[
        jQuery(document).ready(function() {
            jQuery("table.results tbody").click(function(event) {
                if (event.target.nodeName === 'TD') {
                    var target = jQuery(event.target);
                    var parent = jQuery(target.parent('tr')[0]);
                    var rowId = parent.find(">td:first-child").text();
                    var selecting = !parent.hasClass('selected');
                    if (selecting) {
                        jQuery('div.actions form').append('<input type="hidden" name="id" value="' + rowId + '"/>')
                        parent.addClass('selected');
                    } else {
                        jQuery('div.actions form input[value="' + rowId + '"]').remove();
                        parent.removeClass('selected');
                    }
                }
                jQuery('.actions .message').text(jQuery('table.results tr.selected').size() + " rows are selected");
            });
            jQuery('.actions a.selectPage').click(function() {
                jQuery('table.results tbody td:first-child').click();
                return false;
            })
        });
        //]]>
    </script>
</div>
</body>
</html>