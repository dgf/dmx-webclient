/*global requirejs, $, RESTClient */

requirejs.config({ baseUrl: '/dmx/script' })

$(function() { // jQuery ready => start up
    requirejs(['webclient'], function (dmx) {
        // TODO wrap and export DM4 rest client as dm4rest
        dmx.start(new RESTClient('core'))
    })
})
