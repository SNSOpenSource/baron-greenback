<div class="side-menu">
    <div class="views-menu-container">
        $include.({$base$views/menu?current=$meta.view$&query=$urlEncode(htmlDecode(meta.query))$&drills=$urlEncode(htmlDecode(meta.drills))$}).body$
        $include.({$base$facets/facets?current=$meta.view$&query=$urlEncode(htmlDecode(meta.query))$&drills=$urlEncode(htmlDecode(meta.drills))$}).body$
    </div>
    <div class="side-menu-toggle-close"></div>
</div>