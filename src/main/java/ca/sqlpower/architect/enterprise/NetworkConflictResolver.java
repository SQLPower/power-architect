/*
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.enterprise;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.dao.MessageSender;
import ca.sqlpower.dao.PersistedObjectEntry;
import ca.sqlpower.dao.PersistedPropertiesEntry;
import ca.sqlpower.dao.PersistedSPOProperty;
import ca.sqlpower.dao.PersistedSPObject;
import ca.sqlpower.dao.RemovedObjectEntry;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersisterListener;
import ca.sqlpower.dao.SPSessionPersister;
import ca.sqlpower.dao.SPPersister.DataType;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.SPSession;
import ca.sqlpower.util.SQLPowerUtils;

import com.enterprisedt.util.debug.Logger;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class NetworkConflictResolver extends Thread implements MessageSender<JSONObject> {

    private static final Logger logger = Logger.getLogger(NetworkConflictResolver.class);
    private AtomicBoolean postingJSON = new AtomicBoolean(false);
    private boolean updating = false;
    
    private SPPersisterListener listener;
    private SessionPersisterSuperConverter converter;
    private final SPSession session;
    
    private int currentRevision = 0;
    
    private long retryDelay = 1000;
    
    private final SPJSONMessageDecoder jsonDecoder;

    private final ProjectLocation projectLocation;
    private final HttpClient outboundHttpClient;
    private final HttpClient inboundHttpClient;
    
    private String contextRelativePath;

    private volatile boolean cancelled;

    private JSONArray messageBuffer = new JSONArray();
    
    private List<PersistedSPObject> inboundObjectsToAdd = new LinkedList<PersistedSPObject>();
    private Multimap<String, PersistedSPOProperty> inboundPropertiesToChange = LinkedListMultimap.create();
    private List<RemovedObjectEntry> inboundObjectsToRemove = new LinkedList<RemovedObjectEntry>();
    
    private List<PersistedSPObject> outboundObjectsToAdd = new LinkedList<PersistedSPObject>();
    private Multimap<String, PersistedSPOProperty> outboundPropertiesToChange = LinkedListMultimap.create();
    private List<RemovedObjectEntry> outboundObjectsToRemove = new LinkedList<RemovedObjectEntry>();
    
    private List<PersistedObjectEntry> outboundObjectsToAddRollbackList = new LinkedList<PersistedObjectEntry>();
    private List<PersistedPropertiesEntry> outboundPropertiesToChangeRollbackList = new LinkedList<PersistedPropertiesEntry>();
    private List<RemovedObjectEntry> outboundObjectsToRemoveRollbackList = new LinkedList<RemovedObjectEntry>();
    
    private List<UpdateListener> updateListeners = new ArrayList<UpdateListener>();
    
    public NetworkConflictResolver(
            ProjectLocation projectLocation, 
            SPJSONMessageDecoder jsonDecoder, 
            HttpClient inboundHttpClient, 
            HttpClient outboundHttpClient,
            SPSession session) 
    {
        super("updater-" + projectLocation.getUUID());
        
        this.jsonDecoder = jsonDecoder;
        this.projectLocation = projectLocation;
        this.inboundHttpClient = inboundHttpClient;
        this.outboundHttpClient = outboundHttpClient;
        this.session = session;
        
        contextRelativePath = "/project/" + projectLocation.getUUID();
    }
    
    public void setListener(SPPersisterListener listener) {
        this.listener = listener;
    }

    public void setConverter(SessionPersisterSuperConverter converter) {
        this.converter = converter;
    }
    
    public List<UpdateListener> getListeners() {
        return updateListeners;
    }
    
    public void flush() {
        flush(false);
    }
    
    private void flush(boolean reflush) {
        if (postingJSON.get() && !reflush) {
            return;
        } else {
            postingJSON.set(true);
        }
        // Try to send json message ... 
        JSONMessage response = postJsonArray(messageBuffer.toString());
        if (response.isSuccessful()) {
            // Sent json message without conflict. 
            try {
                currentRevision = (new JSONObject(response.getBody())).getInt("currentRevision");
            } catch (JSONException e) {
                throw new RuntimeException("Could not update current revision" + e.getMessage());
            }
            // Prepare for next send ...
            clear(reflush);
        } else {
            // Did not successfully post json, we must update ourselves, and then try again if we can. 
            if (!reflush) {
                // These lists should reflect the state of the workspace at the time of the conflict.
                // The workspace could be updated several times before a successful post is made.
                fillOutboundPersistedLists();
            }
            // Try to rollback our changes
            try {
                session.getWorkspace().rollback("Hello this is a rollback");
            } catch (Exception e) {
                throw new RuntimeException("Reflush failed on rollback", e);
            }
            String json;
            int newRev;
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                json = jsonObject.getString("data");
                newRev = jsonObject.getInt("currentRevision");
            } catch (Exception e) {
                throw new RuntimeException("Reflush failed on getJson", e);
            }
            // Try to create inboundPersistedLists for comparison with the outbound. These will be used
            // for special case collision detection.
            fillInboundPersistedLists(json);
            // Try to apply update
            decodeMessage(json, newRev);
            // We need an additional step here for checking for special case conflicts
            if (detectConflict()) {
                throw new RuntimeException("There is a conflict between our state and the server's, our changes will be lost");
            } else {
                // Try to return the persisted objects to their state pre-update.
                try {
                    SPSessionPersister.redoForSession(session.getWorkspace(), 
                            outboundObjectsToAdd, outboundPropertiesToChange, 
                            outboundObjectsToRemove, converter);
                    // We want to re-send our changes, but only if we were able to restore them
                    flush(true);
                } catch (Exception ex) {
                    throw new RuntimeException("Reflush failed on rollforward", ex);
                }
            }
        }
        postingJSON.set(false);
    }

    public void clear() {
        clear(false);
    }
    
    private void clear(boolean reflush) {
        messageBuffer = new JSONArray();
        
        if (reflush) {
            inboundObjectsToAdd.clear();
            inboundPropertiesToChange.clear();  // XXX does this cause lists to retain old objects?
            inboundObjectsToRemove.clear();
            
            outboundObjectsToAdd.clear();
            outboundPropertiesToChange.clear();
            outboundObjectsToRemove.clear();
            
            outboundObjectsToAddRollbackList.clear();
            outboundPropertiesToChangeRollbackList.clear();
            outboundObjectsToRemoveRollbackList.clear();
        }
    }
    
    public void send(JSONObject content) throws SPPersistenceException {
        messageBuffer.put(content);
    }
    
    @Override
    public void run() {
        try {
            while (!this.isInterrupted() && !cancelled) {
               try { 
                   
                   while (updating) { // this should wait for persisting to server as well.
                       synchronized (this) {
                           wait();
                       }
                   }
                   
                   updating = true;
                   
                   // Request an update from the server using the current revision number.
                   JSONMessage message = getJsonArray(inboundHttpClient);
                   final JSONObject json = new JSONObject(message.getBody());
                   session.runInForeground(new Runnable() {
                       public void run() {
                           try {
                               if (!postingJSON.get()) {
                                   decodeMessage(json.getString("data"), json.getInt("currentRevision"));
                               }
                           } catch (Exception e) {
                               // TODO: Discard corrupt workspace and start again from scratch.
                               interrupt();
                               throw new RuntimeException("Update from server failed! Unable to decode the message: ", e);
                           } finally {
                               synchronized (NetworkConflictResolver.this) {
                                   updating = false;
                                   NetworkConflictResolver.this.notify();
                               }
                           }
                       }
                   });
               } catch (Exception ex) {    
                   logger.error("Failed to contact server. Will retry in " + retryDelay + " ms.", ex);
                   Thread.sleep(retryDelay);
               }
            }
        } catch (InterruptedException ex) {
            logger.info("Updater thread exiting normally due to interruption.");
        }
        
        inboundHttpClient.getConnectionManager().shutdown();
    }
    
    /**
     * Exists for code reuse.
     * 
     * @param jsonArray
     * @param newRevision
     * @throws SPPersistenceException
     */
    private void decodeMessage(String jsonArray, int newRevision) {
        try {
            if (currentRevision < newRevision) {
                // Now we can apply the update ...
                jsonDecoder.decode(jsonArray);
                currentRevision = newRevision;
                
                List<UpdateListener> listenersToRemove = new ArrayList<UpdateListener>();
                for (UpdateListener listener : updateListeners) {
                    if (listener.updatePerformed(this)) {
                        listenersToRemove.add(listener);
                    }
                }
                updateListeners.removeAll(listenersToRemove);
            } 
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode the message: " + jsonArray, e);
        }
    }
    
    public void interrupt() {
        super.interrupt();
        cancelled = true;
    }
    
    private void fillOutboundPersistedLists() {
        for (PersistedSPObject obj : listener.getPersistedObjects()) {
            outboundObjectsToAdd.add(obj);
        }
        for (PersistedSPOProperty prop : listener.getPersistedProperties().values()) {
            outboundPropertiesToChange.put(prop.getUUID(), prop);
        }
        for (RemovedObjectEntry rem : listener.getObjectsToRemove()) {
            outboundObjectsToRemove.add(rem);
        }
        for (PersistedObjectEntry poe : listener.getObjectsRollbackList()) {
            outboundObjectsToAddRollbackList.add(poe);
        }
        for (PersistedPropertiesEntry ppe : listener.getPropertiesRollbackList()) {
            outboundPropertiesToChangeRollbackList.add(ppe);
        }
        for (RemovedObjectEntry roe : listener.getRemovedRollbackList()) {
            outboundObjectsToRemoveRollbackList.add(roe);
        }
    }
    
    private void fillInboundPersistedLists(String json) {
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                
                if (obj.getString("method").equals("persistObject")) {
                    
                    String parentUUID = obj.getString("parentUUID");
                    String type = obj.getString("type");
                    String uuid = obj.getString("uuid");
                    int index = obj.getInt("index");
                    
                    inboundObjectsToAdd.add(new PersistedSPObject(parentUUID, type, uuid, index));
                    
                } else if (obj.getString("method").equals("persistProperty")) {
                    
                    String uuid = obj.getString("uuid");
                    String propertyName = obj.getString("propertyName");
                    DataType type = DataType.valueOf(obj.getString("type"));
                    Object oldValue = null;
                    try {
                        oldValue = SPJSONMessageDecoder.getWithType(obj, type, "oldValue");
                    } catch (Exception e) {}
                    Object newValue = SPJSONMessageDecoder.getWithType(obj, type, "newValue");
                    boolean unconditional = false;
                    
                    PersistedSPOProperty property = new PersistedSPOProperty(uuid, propertyName, type, oldValue, newValue, unconditional);
                    
                    if (inboundPropertiesToChange.keySet().contains(uuid)) {
                        inboundPropertiesToChange.asMap().get(uuid).add(property);
                    } else {
                        inboundPropertiesToChange.put(uuid, property);
                    }
                    
                } else if (obj.getString("method").equals("removeObject")) {
                    
                    String parentUUID = obj.getString("parentUUID");
                    String uuid = obj.getString("uuid");
                    SPObject objectToRemove = SQLPowerUtils.findByUuid(session.getWorkspace(), uuid, SPObject.class);

                    inboundObjectsToRemove.add(new RemovedObjectEntry(parentUUID, objectToRemove, 
                            objectToRemove.getParent().getChildren().indexOf(objectToRemove)));
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to create persisted lists: ", ex);
        }
    }
    
    private boolean detectConflict() {
        // XXX : Special cases are to be looked for here.
        if (checkForSimultaneousEdit()) return true;
        // No special cases found.
        return false;
    }
    
    private boolean checkForSimultaneousEdit() {
        String targetDBuuid = session.getWorkspace().getUUID();
        for (PersistedSPObject inSpo : inboundObjectsToAdd) {
            for (PersistedSPObject outSpo : outboundObjectsToAdd) {
                if (inSpo.getParentUUID().equals(outSpo.getParentUUID()) && !inSpo.equals(targetDBuuid)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Creates and executes an HttpPost request containing the json of whatever
     * transaction was completed last.
     * @param jsonArray Typically created by calling toString() on a JSONArray
     * @return A JSONMessage holding the successfulness and message body of the server's response
     */
    private JSONMessage postJsonArray(String jsonArray) {
        try {
            URI serverURI = new URI("http", null, 
                    projectLocation.getServiceInfo().getServerAddress(), 
                    projectLocation.getServiceInfo().getPort(),
                    projectLocation.getServiceInfo().getPath() + "/project/" + projectLocation.getUUID(), 
                    "currentRevision=" + currentRevision, null);
            HttpPost postRequest = new HttpPost(serverURI);
            postRequest.setEntity(new StringEntity(jsonArray)); 
            postRequest.setHeader("Content-Type", "application/json");
            HttpUriRequest request = postRequest;
            return outboundHttpClient.execute(request, new JSONResponseHandler());
        } catch (Exception ex) {
            throw new RuntimeException("Unable to post json to server: " + jsonArray + "\n"+ ex.getMessage());
        }
    }
    
    /**
     * Creates and executes an HttpGet request for an update from the server.
     * @return A JSONMessage holding the successfulness and message body of the server's response
     */
    private JSONMessage getJsonArray(HttpClient client) {
        try {
            URI uri = new URI("http", null, 
                    projectLocation.getServiceInfo().getServerAddress(), 
                    projectLocation.getServiceInfo().getPort(),
                    projectLocation.getServiceInfo().getPath() + contextRelativePath, 
                    "oldRevisionNo=" + currentRevision, null);
            HttpUriRequest request = new HttpGet(uri);
            return client.execute(request, new JSONResponseHandler());
        } catch (Exception ex) {
            throw new RuntimeException("Unable to get json from server: " + ex.getMessage());
        }
    }
    
    public int getRevision() {
        return currentRevision;
    }

    public void addListener(UpdateListener listener) {
        updateListeners.add(listener);
    }
    
    public static interface UpdateListener {
        // true indicates that the updater should be removed from the list.
        public boolean updatePerformed(NetworkConflictResolver resolver);
    }
}
