(function($, Dm4Client) {

    var dm4 = new Dm4Client('core')

    function getScripts() {
        return dm4.request('GET', '/dmx/scripts').items
    }

    $(function() {
        var scripts = getScripts(), $scripts = $('#scripts')

        $.each(scripts, function(s, script) {
            $scripts.append($('<li>').text(script.name).data('code', script.code))
        })

        $scripts.on('click', 'li', function() {
            eval($(this).data('code'))
        })
    })

})(jQuery, RESTClient)
