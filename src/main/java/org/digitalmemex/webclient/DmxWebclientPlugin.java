package org.digitalmemex.webclient;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.deepamehta.core.DeepaMehtaObject;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
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

    public static final String SCRIPT_TYPE = "dmx.script";

    public static final String SCRIPT_NAME = "dmx.script.name";

    public static final String SCRIPT_CODE = "dmx.script.code";

    private static Logger log = Logger.getLogger(DmxWebclientPlugin.class.getName());

    private AccessControlService acService;

    private boolean isInitialized;

    @GET
    @Path("/scripts")
    @Produces(MediaType.APPLICATION_JSON)
    public ResultSet<Script> getScripts(@HeaderParam("Cookie") ClientState cookie) {
        ResultSet<RelatedTopic> topics = dms.getTopics(SCRIPT_TYPE, true, 0, cookie);
        Set<Script> scripts = new HashSet<Script>();
        for (RelatedTopic script : topics) {
            scripts.add(new Script(//
                    script.getCompositeValue().getString(SCRIPT_NAME), //
                    script.getCompositeValue().getString(SCRIPT_CODE)));
        }
        return new ResultSet<Script>(scripts.size(), scripts);
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
        for (String typeUri : new String[] { SCRIPT_TYPE }) {
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
