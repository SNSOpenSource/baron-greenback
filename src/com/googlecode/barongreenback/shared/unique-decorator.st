$decorator(content={
    <div class="side-menu">
        <div class="views-menu-container">
            $include.({$base$views/menu?current=$meta.view$&query=$urlEncode(htmlDecode(meta.query))$&drills=$urlEncode(htmlDecode(meta.drills))$}).body$
            <div id="quicklinks">
                $div.quicklinks$
            </div>
        </div>
        <div class="side-menu-toggle-close"></div>
    </div>
    <div class="results-data">
        $div.uniquedata$
    </div>
}, contentClass="unique-page")$