/*global jQuery, dm4c*/
(function ($, dm4c) {

  function getMainOfApplication(appId) {
    var scripts = dm4c.restc.get_topic_related_topics(appId, {
      others_role_type_uri: 'dm4.core.child',
      others_topic_type_uri: 'dmx.script'
    }, false, 1)
    if (scripts.total_count === 1) {
        return scripts.items[0] // the first one
    } else {
        return { id: -1 }
    }
  }

  function createScriptMenu(selected, onChoose) {
    var menu = dm4c.ui.menu(onChoose)
    $.each(dm4c.restc.get_topics('dmx.script', false, true).items, function (t, topic) {
      menu.add_item({ label: topic.value, value: topic.id })
    })
    if (selected !== -1) {
      menu.select(selected)
    }
    return menu
  }

  dm4c.add_multi_renderer('dmx.script.renderer', {

    render_info: function (pages, $parent, level) {
      dm4c.render.field_label(pages[0].object_type.value, $parent)
      if (pages[0].object.id !== -1) { // ignore empty default page model
        var main = getMainOfApplication(pages[0].parent.object.id)
        $parent.append($('<div>').text(main.value))
      }
    },

    render_form: function (pages, $parent, level) {
      var deselectedId = -1,
        selectedId = getMainOfApplication(pages[0].parent.object.id).id,
        menu = createScriptMenu(selectedId, function (script) {
          // value contains a new selection?
          if (selectedId !== script.value) {
            if (deselectedId === -1) { // save the old selection once
              deselectedId = selectedId
            } else if (deselectedId === signature.value) {
              // the old one is selected again
              deselectedId = -1
            }
          }
          selectedId = script.value
        })
      $parent.append($('<div>').append(menu.dom))

      return function () { // create aggregation of last selection
        var values = []
        if (selectedId !== -1) {
          values.push(dm4c.REF_PREFIX + selectedId)
          if (deselectedId !== -1) {
            values.push(dm4c.DEL_PREFIX + deselectedId)
          }
        }
        return values
      }
    }
  })
}(jQuery, dm4c))
