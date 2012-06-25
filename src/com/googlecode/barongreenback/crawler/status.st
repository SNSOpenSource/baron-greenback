<html>
<head>
    <!--<meta http-equiv="refresh" content="1"/>-->

</head>
<body>

<canvas id="chart" width="800" height="100"></canvas>

<table>
    <tr>
        <th>Name</th>
        <th>Active Threads</th>
        <th>Queued Jobs</th>
    </tr>
    $executors : {executor |
    <tr>
        <td>$executor.name$</td>
        <td>$executor.activeThreads$</td>
        <td>$executor.size$</td>
    </tr>
    }$
</table>
<script src="http://smoothiecharts.org/smoothie.js"></script>
<script>
    var random = new TimeSeries();
    var maxTime = new Date().getTime();
    window.onload = function () {
        createTimeline();
        setInterval(function () {
            jQuery.get("/stats/crawler", function (data) {
                jQuery.each(data.responseTimes, function (i) {
                    var responseTime = data.responseTimes[i];
                    if (responseTime.date > maxTime) {
                        maxTime = responseTime.date;
                        random.append(responseTime.date, responseTime.timing);
                    }
                });
            });
        }, 1000);
    }

    function createTimeline() {
        var chart = new SmoothieChart();
        chart.addTimeSeries(random, { strokeStyle:'rgba(0, 255, 0, 1)', fillStyle:'rgba(0, 255, 0, 0.2)', lineWidth:4 });
        chart.streamTo(document.getElementById("chart"), 500);
    }
</script>
</body>
</html>