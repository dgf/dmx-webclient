package org.digitalmemex.webclient;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.deepamehta.core.JSONEnabled;

public class Script implements JSONEnabled {

    private final String name;

    private final String code;

    public Script(String name, String code) {
        this.name = name;
        this.code = code;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject image = new JSONObject();
        try {
            image.put("name", name);
            image.put("code", code);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

}
