/*global jQuery, dm4c*/

(function($, dm4c) {

    dm4c.add_simple_renderer('dmx.script.code.renderer', {

        render_info : function(model, $parent) {
            dm4c.render.field_label(model, $parent)
            $parent.append($('<pre>').text(model.value))
        },

        render_form : function(model, $parent) {
            // create editor DOM element
            var editId = 'acEditor' + model.toplevel_object,
                $editor = $('<div>').attr('id', editId).text(model.value)

            // style and append it
            $editor.attr('style', 'position: relative; width: 97%; min-height: 237px;')
            $parent.append($editor)

            // configure editor
            var editor = window.ace.edit(editId)
            editor.getSession().setMode('ace/mode/javascript')
            editor.setTheme('ace/theme/eclipse')

            // return editor value on submit
            return function () {
                return editor.getValue()
            }
        }
    })
}(jQuery, dm4c))
