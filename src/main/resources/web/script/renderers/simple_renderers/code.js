/*global jQuery, dm4c*/

(function($, dm4c) {

    function createAceEditor(editId, model) {
        // configure editor
        var editor = window.ace.edit(editId)
        editor.setFontSize('17px')
        editor.setTheme('ace/theme/eclipse')

        // configure mode
        var name = model.toplevel_object.value
        if (name.match("\.js$") == '.js') {
            editor.getSession().setMode('ace/mode/javascript')
        } else if (name.match("\.coffee$") == '.coffee') {
            editor.getSession().setMode('ace/mode/coffee')
        } else if (name.match("\.jade$") == '.jade') {
            editor.getSession().setMode('ace/mode/jade')
        } else if (name.match("\.css$") == '.css') {
            editor.getSession().setMode('ace/mode/css')
        } else if (name.match("\.less$") == '.less') {
            editor.getSession().setMode('ace/mode/less')
        } else {
            editor.getSession().setMode('ace/mode/text')
        }

        return editor
    }

    function createEditorDiv(editId, value) {
        return $('<div>').attr('id', editId).text(value)
                         .attr('style', 'position: relative; width: 97%; height: 437px')
    }

    dm4c.add_simple_renderer('dmx.script.code.renderer', {

        render_info : function(model, $parent) {
            dm4c.render.field_label(model, $parent)
            //$parent.append($('<pre>').text(model.value))
            var id = 'dmxEditor' + model.toplevel_object.id
            $parent.append(createEditorDiv(id, model.value))
            createAceEditor(id, model).setReadOnly(true)
        },

        render_form : function(model, $parent) {
            var id = 'dmxEditor' + model.toplevel_object.id
            $parent.append(createEditorDiv(id, model.value))
            var editor = createAceEditor(id, model)

            // return editor value on submit
            return function () {
                return editor.getValue()
            }
        }
    })
}(jQuery, dm4c))
