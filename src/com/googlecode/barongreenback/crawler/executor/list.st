<!DOCTYPE html>
<html>
<head>
    <title>Executor Config</title>
</head>
<body>
$message()$
<div>
<form method="POST" action="update" >

    <div>
        $group(name="Input Handler", content={
            $input(name="inputHandlerThreads", label="Threads", value=InputHandlerThreads, placeholder="", message="")$
            $input(name="inputHandlerCapacity", label="Capacity", value=InputHandlerCapacity, placeholder="", message="")$
        })$

   </div>
    <div>
        $group(name="Process Handler", content={
             $input(name="processHandlerThreads", label="Threads", class="name", value=ProcessHandlerThreads, placeholder="", message="")$
             $input(name="processHandlerCapacity", label="Capacity", class="name", value=ProcessHandlerCapacity, placeholder="", message="")$
        })$
    </div>
    <div>
        $group(name="Output Handler", content={
             $input(name="outputHandlerThreads", label="Threads", class="name", value=OutputHandlerThreads, placeholder="", message="")$
             $input(name="outputHandlerCapacity", label="Capacity", class="name", value=OutputHandlerCapacity, placeholder="", message="")$
        })$
    </div>
    <div class="actions">
        A value of 0 for capacity represents unbounded.
    </div>
    <div class="actions">
        <input type="submit" value="Save" name="action" class="save"/>
        <a href="list" class="cancel">Cancel</a>
    </div>
</form>
</div>
</body>
</html>