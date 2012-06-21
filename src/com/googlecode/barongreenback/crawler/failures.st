<!DOCTYPE html>
<html>
<head>
    <title>Crawler Failures</title>
</head>
<body class="failures">

<table class="shouldExpand results zebra-striped">
    $thead(headers=["Uri", "Response", "Actions"])$
    <tbody>
    $failures: { failure |
    <tr>
        <td><a href="$failure.uri$">$failure.uri$</a></td>
        <td><div class="entity">$failure.response$</div></td>
        <td>
            <form action="/crawler/failures/retry" method="get">
                <input type="hidden" name="id" value="$failure.id$"/>
                <input type="submit" value="retry" class="btn"/>
            </form>
        </td>
    </tr>
    }$
    </tbody>
</table>

</body>
</html>