dm4c.add_plugin('dmx.plugin', function () {
    var baseUrl = '/org.digitalmemex.dmx-webclient/script/vendor/'
    dm4c.load_script(baseUrl + 'ace.js')
    dm4c.load_script(baseUrl + 'ace/mode-coffee.js')
    dm4c.load_script(baseUrl + 'ace/mode-jade.js')
    dm4c.load_script(baseUrl + 'ace/mode-javascript.js')
    dm4c.load_script(baseUrl + 'ace/mode-text.js')
    dm4c.load_script(baseUrl + 'ace/theme-eclipse.js')
    dm4c.load_script(baseUrl + 'ace/theme-textmate.js')
    window.ace.config.set('workerPath', baseUrl + 'ace')
})
