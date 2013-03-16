package org.digitalmemex.webclient;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sun.jersey.api.view.Viewable;

import de.deepamehta.core.DeepaMehtaObject;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.core.service.accesscontrol.ACLEntry;
import de.deepamehta.core.service.accesscontrol.AccessControlList;
import de.deepamehta.core.service.accesscontrol.Operation;
import de.deepamehta.core.service.accesscontrol.UserRole;
import de.deepamehta.core.service.annotation.ConsumesService;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.plugins.webactivator.WebActivatorPlugin;

@Path("/dmx")
public class DmxWebclientPlugin extends WebActivatorPlugin {

    public static final String APP = "dmx.app";

    public static final String APP_NAME = "dmx.app.name";

    public static final String APP_PATH = "dmx.app.path";

    public static final String SCRIPT = "dmx.script";

    public static final String SCRIPT_NAME = "dmx.script.name";

    public static final String SCRIPT_CODE = "dmx.script.code";

    private static Logger log = Logger.getLogger(DmxWebclientPlugin.class.getName());

    private AccessControlService acService;

    private boolean isInitialized;

    private String getRequireTag(String name) {
        if (name.endsWith(".js")) {
            return name.replaceAll(".js", "");
        } else if (name.endsWith(".coffee")) {
            return "cs!" + name.replaceAll(".coffee", "");
        } else {
            throw new IllegalArgumentException("only javascript and coffescript supported");
        }
    }

    /**
     * @return index page
     */
    @GET
    @Produces("text/html")
    public Viewable index(@HeaderParam("Cookie") ClientState cookie) {
        log.info("open application list");
        context.setVariable("title", "Digital MemeX Webclient");
        context.setVariable("script", getRequireTag("apps.coffee"));
        return view("index");
    }

    /**
     * @return index page
     */
    @GET
    @Path("/app/{path}")
    @Produces("text/html")
    public Viewable app(@PathParam("path") String path, @HeaderParam("Cookie") ClientState cookie) {
        log.info("open dmx application " + path);
        Topic appPath = dms.getTopic(APP_PATH, new SimpleValue(path), true, cookie);
        RelatedTopic app = appPath.getRelatedTopic("dm4.core.composition", //
                "dm4.core.child", "dm4.core.parent", APP, true, false, cookie);
        String script = app.getCompositeValue().getTopics(SCRIPT).get(0) // first
                .getCompositeValue().getString(SCRIPT_NAME);

        context.setVariable("title", app.getCompositeValue().getString(APP_NAME));
        context.setVariable("script", getRequireTag(script));
        return view("index");
    }

    @GET
    @Path("/script/{name}")
    @Produces("text/javascript")
    public String getScript(@PathParam("name") String name, @HeaderParam("Cookie") ClientState cookie) {
        log.info("load DMX script " + name);
        Topic scriptName = dms.getTopic(SCRIPT_NAME, new SimpleValue(name), true, cookie);
        RelatedTopic script = scriptName.getRelatedTopic("dm4.core.composition", //
                "dm4.core.child", "dm4.core.parent", SCRIPT, true, false, cookie);
        return script.getCompositeValue().getString(SCRIPT_CODE);
    }

    @Override
    @ConsumesService({ "de.deepamehta.plugins.accesscontrol.service.AccessControlService" })
    public void serviceArrived(PluginService service) {
        if (service instanceof AccessControlService) {
            acService = (AccessControlService) service;
        }
        configureIfReady();
    }

    @Override
    public void init() {
        setupRenderContext();
        isInitialized = true;
        configureIfReady();
    }

    private void configureIfReady() {
        if (isInitialized && acService != null) {
            checkACLsOfMigration();
        }
    }

    private void checkACLsOfMigration() {
        for (String typeUri : new String[] { APP, SCRIPT }) {
            checkACLsOfTopics(typeUri);
        }
    }

    private void checkACLsOfTopics(String typeUri) {
        for (RelatedTopic topic : dms.getTopics(typeUri, false, 0, null)) {
            checkACLsOfObject(topic);
        }
    }

    private void checkACLsOfObject(DeepaMehtaObject instance) {
        if (acService.getCreator(instance.getId()) == null) {
            log.info("initial ACL update " + instance.getId() + ": " + instance.getSimpleValue());
            Topic admin = acService.getUsername("admin");
            String adminName = admin.getSimpleValue().toString();
            acService.setCreator(instance.getId(), adminName);
            acService.setOwner(instance.getId(), adminName);
            acService.setACL(instance.getId(), new AccessControlList( //
                    new ACLEntry(Operation.WRITE, UserRole.OWNER)));
        }
    }

}
