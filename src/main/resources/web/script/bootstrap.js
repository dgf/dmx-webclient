/*global define, requirejs, $, RESTClient*/

define('jQuery', ['/de.deepamehta.webclient/script/vendor/jquery/jquery-1.7.2.min.js'], function () {
    return jQuery // wrap jQuery
})

define('dm4rest', ['/de.deepamehta.webclient/script/util/rest_client.js'], function () {
    return new RESTClient('/core') // wrap DeepaMehta 4 REST client utility
})

define('d3', ['/org.digitalmemex.dmx-webclient/script/vendor/d3.min.js'], function () {
    return d3 // wrap d3
})

requirejs.config({ baseUrl: '/org.digitalmemex.dmx-webclient/script/vendor' })
requirejs(['jQuery', 'd3', 'knockout', 'domReady!', 'cs', 'jade', 'css', 'lessc', 'less'],
function ($) { // d3, knockout and require plugins ready
    requirejs.config({ baseUrl: '/dmx/script' })
    $(function() { // jQuery ready => start up
        var $dmx = $('#dmx')
        requirejs([$dmx.text()], function (dmx) {
            dmx.start($dmx.empty())
            $dmx.show()
        })
    })
})
