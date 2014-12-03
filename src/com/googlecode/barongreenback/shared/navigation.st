<div class="views-menu">
    $include.({$base$views/menu?current=$meta.view$&query=$urlEncode(htmlDecode(meta.query))$&drills=$urlEncode(htmlDecode(meta.drills))$}).body$
    $include.({$base$facets/facets?current=$meta.view$&query=$urlEncode(htmlDecode(meta.query))$&drills=$urlEncode(htmlDecode(meta.drills))$}).body$
</div>
