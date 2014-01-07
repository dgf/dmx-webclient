package org.digitalmemex.webclient;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sun.jersey.api.view.Viewable;

import de.deepamehta.core.DeepaMehtaObject;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.plugins.accesscontrol.model.ACLEntry;
import de.deepamehta.plugins.accesscontrol.model.AccessControlList;
import de.deepamehta.plugins.accesscontrol.model.Operation;
import de.deepamehta.plugins.accesscontrol.model.UserRole;
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

    private String adminName = null;

    private String getRequireTag(String name) {
        if (name.endsWith(".js")) {
            return name.replaceAll(".js", "");
        } else if (name.endsWith(".coffee")) {
            return "cs!" + name.replaceAll(".coffee", "");
        } else {
            throw new IllegalArgumentException("only supports JavaScript and CoffeeScript");
        }
    }

    /**
     * @return index page
     */
    @GET
    @Produces("text/html")
    public Viewable index() {
        log.info("open application list");
        viewData("title", "Digital MemeX Webclient");
        viewData("script", getRequireTag("apps.coffee"));
        return view("index");
    }

    /**
     * @return index page
     */
    @GET
    @Path("/app/{path}")
    @Produces("text/html")
    public Viewable app(@PathParam("path") String path) {
        log.info("open dmx application " + path);
        Topic appPath = dms.getTopic(APP_PATH, new SimpleValue(path), false);
        RelatedTopic app = appPath.getRelatedTopic("dm4.core.composition", //
                "dm4.core.child", "dm4.core.parent", APP, true, false);
        String script = app.getCompositeValue().getTopics(SCRIPT).get(0) // first
                .getCompositeValue().getString(SCRIPT_NAME);

        viewData("title", app.getCompositeValue().getString(APP_NAME));
        viewData("script", getRequireTag(script));
        return view("index");
    }

    @GET
    @Path("/script/{name}")
    @Produces("text/javascript")
    public String getScript(@PathParam("name") String name) {
        log.info("load DMX script " + name);
        Topic scriptName = dms.getTopic(SCRIPT_NAME, new SimpleValue(name), false);
        RelatedTopic script = scriptName.getRelatedTopic("dm4.core.composition", //
                "dm4.core.child", "dm4.core.parent", SCRIPT, true, false);
        return script.getCompositeValue().getString(SCRIPT_CODE);
    }

    @Override
    @ConsumesService({"de.deepamehta.plugins.accesscontrol.service.AccessControlService"})
    public void serviceArrived(PluginService service) {
        if (service instanceof AccessControlService) {
            acService = (AccessControlService) service;
        }
        configureIfReady();
    }

    @Override
    public void init() {
        initTemplateEngine();
        isInitialized = true;
        configureIfReady();
    }

    private void configureIfReady() {
        if (isInitialized && acService != null) {
            checkACLsOfMigration();
        }
    }

    private void checkACLsOfMigration() {
        for (String typeUri : new String[]{APP, SCRIPT}) {
            checkACLsOfTopics(typeUri);
        }
    }

    private void checkACLsOfTopics(String typeUri) {
        for (RelatedTopic topic : dms.getTopics(typeUri, false, 0)) {
            checkACLsOfObject(topic);
        }
    }

    private void checkACLsOfObject(DeepaMehtaObject instance) {
        if (acService.getCreator(instance) == null) {
            log.info("initial ACL update " + instance.getId() + ": " + instance.getSimpleValue());
            String adminName = getAdminName();
            acService.setCreator(instance, adminName);
            acService.setOwner(instance, adminName);
            acService.setACL(instance, new AccessControlList(new ACLEntry(Operation.WRITE, UserRole.OWNER)));
        }
    }

    private String getAdminName() {
        if (adminName == null) {
            adminName = acService.getUsername("admin").getSimpleValue().toString();
        }
        return adminName;
    }

}
