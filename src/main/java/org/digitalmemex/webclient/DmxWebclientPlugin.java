package org.digitalmemex.webclient;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.deepamehta.core.DeepaMehtaObject;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.core.service.accesscontrol.ACLEntry;
import de.deepamehta.core.service.accesscontrol.AccessControlList;
import de.deepamehta.core.service.accesscontrol.Operation;
import de.deepamehta.core.service.accesscontrol.UserRole;
import de.deepamehta.core.service.annotation.ConsumesService;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;

@Path("/dmx")
public class DmxWebclientPlugin extends PluginActivator {

    public static final String APP_TYPE = "dmx.app";

    public static final String SCRIPT_TYPE = "dmx.script";

    public static final String SCRIPT_NAME = "dmx.script.name";

    public static final String SCRIPT_CODE = "dmx.script.code";

    private static Logger log = Logger.getLogger(DmxWebclientPlugin.class.getName());

    private AccessControlService acService;

    private boolean isInitialized;

    @GET
    @Path("/script/{name}")
    @Produces("text/javascript")
    public String getScript(@PathParam("name") String name, @HeaderParam("Cookie") ClientState cookie) {
        log.info("load DMX script " + name);
        Topic scriptName = dms.getTopic(SCRIPT_NAME, new SimpleValue(name), true, cookie);
        RelatedTopic script = scriptName.getRelatedTopic("dm4.core.composition", //
                "dm4.core.part", "dm4.core.whole", SCRIPT_TYPE, true, false, cookie);
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
        isInitialized = true;
        configureIfReady();
    }

    private void configureIfReady() {
        if (isInitialized && acService != null) {
            checkACLsOfMigration();
        }
    }

    private void checkACLsOfMigration() {
        for (String typeUri : new String[] { APP_TYPE, SCRIPT_TYPE }) {
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
            acService.createACL(instance.getId(), new AccessControlList( //
                    new ACLEntry(Operation.WRITE, UserRole.OWNER)));
        }
    }

}
