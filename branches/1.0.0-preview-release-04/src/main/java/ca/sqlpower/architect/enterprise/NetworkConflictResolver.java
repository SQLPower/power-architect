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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.AccessDeniedException;

import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.dao.MessageSender;
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
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

import com.enterprisedt.util.debug.Logger;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class NetworkConflictResolver extends Thread implements MessageSender<JSONObject> {

    /**
     * If conflicts are found when trying to decide if an incoming change
     * conflicts with the last user change only this many conflicting properties
     * or reasons will be displayed at max to prevent the dialog from growing
     * too large.
     */
    private static final int MAX_CONFLICTS_TO_DISPLAY = 10;

    private static final Logger logger = Logger.getLogger(NetworkConflictResolver.class);
    private AtomicBoolean postingJSON = new AtomicBoolean(false);
    private boolean updating = false;
    
    private SPPersisterListener listener;
    private SessionPersisterSuperConverter converter;
    private ArchitectClientSideSession session;
    
    private UserPrompterFactory upf;
    
    private int currentRevision = 0;
    
    private long retryDelay = 1000;
    
    private final SPJSONMessageDecoder jsonDecoder;

    private final ProjectLocation projectLocation;
    private final HttpClient outboundHttpClient;
    private final HttpClient inboundHttpClient;
    
    private String contextRelativePath;

    private volatile boolean cancelled;

    private JSONArray messageBuffer = new JSONArray();
    
    private HashMap<String, PersistedSPObject> inboundObjectsToAdd = new HashMap<String, PersistedSPObject>();
    private Multimap<String, PersistedSPOProperty> inboundPropertiesToChange = LinkedListMultimap.create();
    private HashMap<String, RemovedObjectEntry> inboundObjectsToRemove = new HashMap<String, RemovedObjectEntry>();
    
    private Map<String, PersistedSPObject> outboundObjectsToAdd = new LinkedHashMap<String, PersistedSPObject>();
    private Multimap<String, PersistedSPOProperty> outboundPropertiesToChange = LinkedListMultimap.create();
    private Map<String, RemovedObjectEntry> outboundObjectsToRemove = new LinkedHashMap<String, RemovedObjectEntry>();    
    
    private List<UpdateListener> updateListeners = new ArrayList<UpdateListener>();
    
    public NetworkConflictResolver(
            ProjectLocation projectLocation, 
            SPJSONMessageDecoder jsonDecoder, 
            HttpClient inboundHttpClient, 
            HttpClient outboundHttpClient,
            ArchitectClientSideSession session) 
    {
        super("updater-" + projectLocation.getUUID());
        
        this.jsonDecoder = jsonDecoder;
        this.projectLocation = projectLocation;
        this.inboundHttpClient = inboundHttpClient;
        this.outboundHttpClient = outboundHttpClient;
        this.session = session;
        
        contextRelativePath = "/" + ArchitectClientSideSession.REST_TAG + "/project/" + projectLocation.getUUID();
    }
    
    public void setUserPrompterFactory(UserPrompterFactory promptSession) {
        this.upf = promptSession;
    }
    
    public UserPrompterFactory getUserPrompterFactory() {
        return upf;
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
        }
        try {
            postingJSON.set(true);
            // Try to send json message ...
            JSONMessage response = null;
            try {
                response = postJsonArray(messageBuffer.toString());
            } catch (AccessDeniedException e) {
                List<UpdateListener> listenersToRemove = new ArrayList<UpdateListener>();
                for (UpdateListener listener : updateListeners) {
                    if (listener.updateException(NetworkConflictResolver.this)) {
                        listenersToRemove.add(listener);
                    }
                }
                updateListeners.removeAll(listenersToRemove);
                if (upf != null) {
                    upf.createUserPrompter(
                            "You do not have sufficient privileges to perform that action. " +
                            "Please hit the refresh button to synchonize with the server.", 
                            UserPromptType.MESSAGE, 
                            UserPromptOptions.OK, 
                            UserPromptResponse.OK, 
                            "OK", "OK").promptUser("");
                } else {
                    throw e;
                }
                return;
            }
            if (response.isSuccessful()) {
                // Sent json message without conflict.
                try {
                    currentRevision = (new JSONObject(response.getBody())).getInt("currentRevision");
                } catch (JSONException e) {
                    throw new RuntimeException("Could not update current revision" + e.getMessage());
                }
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
                
                //If the preconditions failed which caused the persist to fail don't try to 
                //push the persist forward again.
                if (!response.isSuccessful() && response.getStatusCode() == 412) {
                    logger.info("Friendly error occurred, " + response);
                    if (upf != null) {
                        upf.createUserPrompter(
                                response.getBody(), 
                                UserPromptType.MESSAGE, 
                                UserPromptOptions.OK, 
                                UserPromptResponse.OK, 
                                "OK", "OK").promptUser("");
                    } else {
                        logger.warn("Missing a prompt session! Message was " + response);
                    }
                    return;
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
                List<ConflictMessage> conflicts = detectConflicts();
                if (conflicts.size() == 0) {
                    // Try to return the persisted objects to their state pre-update.
                    try {
                        SPSessionPersister.redoForSession(session.getWorkspace(), 
                                new LinkedList<PersistedSPObject>(outboundObjectsToAdd.values()),
                                outboundPropertiesToChange, 
                                new LinkedList<RemovedObjectEntry>(outboundObjectsToRemove.values()), converter);
                        // We want to re-send our changes, but only if we were able to restore them
                        flush(true);
                    } catch (Exception ex) {
                        throw new RuntimeException("Reflush failed on rollforward", ex);
                    }
                } else {
                    String message = "";
                    message += "Your changes have been discarded due to a conflict between you and another user: \n";
                    for (int i = 0; i < MAX_CONFLICTS_TO_DISPLAY && i < conflicts.size(); i++) {
                        message += conflicts.get(i).getMessage() + "\n";
                    }
                    session.createUserPrompter(message, 
                            UserPromptType.MESSAGE, 
                            UserPromptOptions.OK, 
                            UserPromptResponse.OK, 
                            "OK", "OK").promptUser("");
                }
            }
        } finally {
            postingJSON.set(false);
            clear(true);
        }
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
                   
                   // Status 410 (Gone) means the workspace was deleted                   
                   if (message.getStatusCode() == 410) {
                       for (UpdateListener listener : updateListeners) {
                           listener.workspaceDeleted();                           
                       }
                       updateListeners.clear();
                       interrupt();
                   }                   
                   // The updater may have been interrupted/closed/deleted while waiting for an update.
                   if (this.isInterrupted() || cancelled) break;
                   
                   final JSONObject json = new JSONObject(message.getBody());
                   session.runInForeground(new Runnable() {
                       public void run() {
                           try {
                               if (!postingJSON.get()) {
                                   decodeMessage(json.getString("data"), json.getInt("currentRevision"));
                               }
                           } catch (AccessDeniedException ade) {
                               interrupt();
                               List<UpdateListener> listenersToRemove = new ArrayList<UpdateListener>();
                               for (UpdateListener listener : updateListeners) {
                                   if (listener.updateException(NetworkConflictResolver.this)) {
                                       listenersToRemove.add(listener);
                                   }
                               }
                               updateListeners.removeAll(listenersToRemove);
                               if (upf != null) {
                                   upf.createUserPrompter(
                                           "You do not have sufficient privileges to perform that action. " +
                                           "Please hit the refresh button to synchronize with the server.", 
                                           UserPromptType.MESSAGE, 
                                           UserPromptOptions.OK, 
                                           UserPromptResponse.OK, 
                                           "OK", "OK").promptUser("");
                               } else {
                                   throw ade;
                               }
                           } catch (Exception e) {
                               // TODO: Discard corrupt workspace and start again from scratch.
                               interrupt();
                               List<UpdateListener> listenersToRemove = new ArrayList<UpdateListener>();
                               for (UpdateListener listener : updateListeners) {
                                   if (listener.updateException(NetworkConflictResolver.this)) {
                                       listenersToRemove.add(listener);
                                   }
                               }
                               updateListeners.removeAll(listenersToRemove);
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
                for (UpdateListener listener : updateListeners) {
                    listener.preUpdatePerformed(NetworkConflictResolver.this);
                }
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
            throw new RuntimeException("Failed to decode the message from the server.", e);
        }
    }
    
    public void interrupt() {
        super.interrupt();
        cancelled = true;
    }
    
    private void fillOutboundPersistedLists() {
        for (PersistedSPObject obj : listener.getPersistedObjects()) {
            outboundObjectsToAdd.put(obj.getUUID(), obj);
        }
        for (PersistedSPOProperty prop : listener.getPersistedProperties()) {
            outboundPropertiesToChange.put(prop.getUUID(), prop);
        }
        for (RemovedObjectEntry rem : listener.getObjectsToRemove().values()) {
            outboundObjectsToRemove.put(rem.getRemovedChild().getUUID(), rem);
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
                    
                    inboundObjectsToAdd.put(uuid, new PersistedSPObject(parentUUID, type, uuid, index));
                    
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

                    inboundObjectsToRemove.put(uuid, new RemovedObjectEntry(parentUUID, objectToRemove, 
                            objectToRemove.getParent().getChildren().indexOf(objectToRemove)));
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to create persisted lists: ", ex);
        }
    }
    
    private List<ConflictMessage> detectConflicts() {
        List<ConflictMessage> conflicts = checkForSimultaneousEdit();
        // ----- Special cases -----
        allowSimultaneousAdditionsUnderDB(conflicts);
        disallowColumnMappingsPointingToSameColumn(conflicts);
        return conflicts;
    }

    /**
     * This method will make sure that column mappings of the same relationship
     * do not point to the same column of a table, since this is illegal.
     */
    private void disallowColumnMappingsPointingToSameColumn(List<ConflictMessage> conflicts) {
        /**
         * Stores the uuids of columns that are being pointed to
         * by column mappings. Will store them like so: relationshipId:columnId
         */
        Set<String> duplicates = getColumnMappingChanges(outboundPropertiesToChange);
        duplicates.retainAll(getColumnMappingChanges(inboundPropertiesToChange));
        for (String duplicate : duplicates) {
            String[] ids = duplicate.split("\\:");
            String relationshipId = ids[0];
            String columnId = ids[1];
            String relationshipName = session.getWorkspace().getObjectInTree(relationshipId).getName();
            String columnName = session.getWorkspace().getObjectInTree(columnId).getName();
            String message = "More than one column mapping of relationship " +
                              relationshipName + " points to the column " + columnName;
            conflicts.add(new ConflictMessage(
                    message, ConflictCase.SPECIAL_CASE, relationshipId, columnId));
        }
        
    }
    
    /**
     * Returns a set of strings indicating which columns were pointed to
     * as a result of column mapping property changes, found in the given properties map.
     * The format of the strings are {relationshipId}:{columnId}
     */
    private Set<String> getColumnMappingChanges(Multimap<String, PersistedSPOProperty> properties) {
        Set<String> changes = new HashSet<String>();        
        for (String uuid : properties.keySet()) {
            Class type;
            String parentId;       
            SPObject spo = session.getWorkspace().getObjectInTree(uuid);
            PersistedSPObject o = outboundObjectsToAdd.get(uuid);
            try {
                if (spo != null) {
                    type = spo.getClass();
                    parentId = spo.getParent().getUUID();
                } else if (o != null) {                    
                    type = Class.forName(o.getType(), true, NetworkConflictResolver.class.getClassLoader());
                    parentId = o.getParentUUID();
                } else {
                    continue;
                }
                if (ColumnMapping.class.isAssignableFrom(type)) {
                    for (PersistedSPOProperty p : properties.get(uuid)) {
                        if (p.getDataType() == DataType.REFERENCE) {
                            changes.add(parentId + ":" + (String) p.getNewValue());
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return changes;
    }

    /**
     * This method will iterate over the given conflicts looking for those of
     * type simulatenous addition. If this conflict was due to a simulatenous
     * addition under the target database or the play pen content pane,
     * the conflict is removed from the list and the index of the call is fixed.
     */
    private void allowSimultaneousAdditionsUnderDB(List<ConflictMessage> conflicts) {
        Iterator<ConflictMessage> iterator = conflicts.iterator();
        List<PersistedSPObject> indexUpdates = new LinkedList<PersistedSPObject>();
        while (iterator.hasNext()) {
            ConflictMessage conflict = iterator.next();
            if (conflict.getConflictCase() == ConflictCase.SIMULTANEOUS_ADDITION) {
                PersistedSPObject o = outboundObjectsToAdd.get(conflict.getObjectId(0));                
                SPObject parent = session.getWorkspace().getObjectInTree(o.getParentUUID());                
                if (parent == session.getTargetDatabase()) {
                    iterator.remove();
                    int size = session.getTargetDatabase().getChildren().size();                     
                    indexUpdates.add(new PersistedSPObject(
                            o.getParentUUID(), o.getType(), o.getUUID(), size));                    
                } else if (parent == session.getWorkspace().getPlayPenContentPane()) {
                    iterator.remove();
                    PlayPenContentPane cp = session.getWorkspace().getPlayPenContentPane();
                    try {
                        Class<PlayPenComponent> type = (Class<PlayPenComponent>) Class.forName(o.getType(), true, NetworkConflictResolver.class.getClassLoader());
                        int newIndex = -1;
                        if (PlayPenContentPane.isDependentComponentType(type)) {
                            if (o.getIndex() < cp.getFirstDependentComponentIndex()) {
                                newIndex = cp.getChildren().size();
                            }
                        } else {
                            if (o.getIndex() >= cp.getFirstDependentComponentIndex()
                                    && o.getIndex() > 0) {
                                newIndex = cp.getFirstDependentComponentIndex() - 1;
                            }
                        }                        
                        if (newIndex > -1) {
                            indexUpdates.add(new PersistedSPObject(
                                o.getParentUUID(), o.getType(), o.getUUID(), newIndex));
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        for (PersistedSPObject o : indexUpdates) {
            outboundObjectsToAdd.put(o.getUUID(), o);
        }
    }

    /**
     * Goes through all the inbound and outbound change lists and
     * determines whether the outbound changes should be allowed to continue.
     * The reasons to prevent the outbound changes are usually cases where
     * as a result of the incoming change, the outbound change would not be
     * possible through the UI anymore, and/or are impossible in such a state.
     * 
     * See ConflictCase for all the cases that are looked for in this method.
     * A Google Docs spreadsheet called Conflict rules has been shared
     * with the psc group. For more information, see that.
     */
    private List<ConflictMessage> checkForSimultaneousEdit() {                        
        
        List<ConflictMessage> conflicts = new LinkedList<ConflictMessage>();
        
        Set<String> inboundAddedObjectParents = new HashSet<String>();
        Set<String> inboundRemovedObjectParents = new HashSet<String>();
        
        Set<String> inboundChangedObjects = new HashSet<String>();
        HashMap<String, String> inboundCreatedDependencies = new HashMap<String, String>();
        
        Set<String> duplicateMoves = new HashSet<String>();
        

        // ----- Populate the inbound sets / maps -----        
        
        for (String uuid : inboundPropertiesToChange.keys()) {
            inboundChangedObjects.add(uuid);
            for (PersistedSPOProperty p : inboundPropertiesToChange.get(uuid)) {
                if (p.getDataType() == DataType.REFERENCE) {
                    inboundCreatedDependencies.put((String) p.getNewValue(), p.getUUID()); 
                }
            }
        }
        
        for (PersistedSPObject o : inboundObjectsToAdd.values()) {
            inboundAddedObjectParents.add(o.getParentUUID());
        }      
        
        for (RemovedObjectEntry o : inboundObjectsToRemove.values()) {          
            inboundRemovedObjectParents.add(o.getParentUUID());
        }
        
        // ----- Iterate through outbound additions -----
        
        Set<String> checkedIfCanAddToTree = new HashSet<String>();        
        Iterator<PersistedSPObject> addedObjects = outboundObjectsToAdd.values().iterator();
        while (addedObjects.hasNext()) {
            PersistedSPObject o = addedObjects.next();            
            
            // Can't add object to a parent that already had a child added or removed.
            // This will also include incoming and/or outgoing moves, which are conflicts too.
            if (inboundAddedObjectParents.contains(o.getParentUUID()) || 
                    inboundRemovedObjectParents.contains(o.getParentUUID())) {              
                conflicts.add(new ConflictMessage(ConflictCase.SIMULTANEOUS_ADDITION, 
                        o.getUUID(), getPersistedObjectName(o)));
            }
            
            // Can't add an object if the direct parent was changed.
            if (inboundChangedObjects.contains(o.getParentUUID())) {
                conflicts.add(new ConflictMessage(ConflictCase.ADDITION_UNDER_CHANGE, 
                        o.getUUID(), getPersistedObjectName(o), 
                        o.getParentUUID(), session.getWorkspace().getObjectInTree(o.getParentUUID()).getName()));
            }
            
            // Make sure we are not adding an object that had an ancestor removed.
            // First iterate up ancestors that are being added in the same transaction.
            PersistedSPObject highestAddition = o;
            while (outboundObjectsToAdd.containsKey(highestAddition.getParentUUID()) &&
                    !checkedIfCanAddToTree.contains(highestAddition.getParentUUID())) {
                checkedIfCanAddToTree.add(highestAddition.getUUID());
                highestAddition = outboundObjectsToAdd.get(highestAddition.getParentUUID());                
            }
            checkedIfCanAddToTree.add(highestAddition.getUUID());
            if (checkedIfCanAddToTree.add(highestAddition.getParentUUID()) &&
                    session.getWorkspace().getObjectInTree(highestAddition.getParentUUID()) == null) {
                conflicts.add(new ConflictMessage(ConflictCase.ADDITION_UNDER_REMOVAL, 
                        highestAddition.getUUID(), getPersistedObjectName(highestAddition)));
            }
            
            // Check if both clients are adding the same object.
            // It could mean they both undid a deletion of this object,
            // or are both trying to move the same object.
            // If they are identical, remove the outbound add from this list.
            // If it was a move and has a corresponding remove call, that
            // must be taken care of in the following outbound removals loop.
            if (inboundObjectsToAdd.containsKey(o.getUUID())) {
                if (inboundObjectsToAdd.get(o.getUUID()).equals(o)) {
                    addedObjects.remove();
                    outboundPropertiesToChange.removeAll(o.getUUID());
                    duplicateMoves.add(o.getUUID());
                } else {
                    conflicts.add(new ConflictMessage(ConflictCase.DIFFERENT_MOVE, 
                            o.getUUID(), getPersistedObjectName(o)));
                }
            }                             
        }
        
        
        // ----- Iterate through outbound removals -----
             
        Iterator<RemovedObjectEntry> removedObjects = outboundObjectsToRemove.values().iterator();        
        while (removedObjects.hasNext()) {
            RemovedObjectEntry object = removedObjects.next();
            final String uuid = object.getRemovedChild().getUUID();
            
            // Check if the object the outbound client is trying to remove does not exist.
            SPObject removedObject = session.getWorkspace().getObjectInTree(uuid);
            if (removedObject == null) {
                // Check if this remove has a corresponding add, meaning it is a move.
                // The incoming remove will override the outgoing move.
                if (outboundObjectsToAdd.containsKey(uuid)) {
                    conflicts.add(new ConflictMessage(ConflictCase.MOVE_OF_REMOVED, 
                            object.getRemovedChild().getUUID(), object.getRemovedChild().getName()));
                } else {
                    // Both clients removed the same object, either directly or indirectly.
                    removedObjects.remove();
                }
            } else if (inboundCreatedDependencies.containsKey(uuid)) {
                // Can't remove an object that was just made a dependency
                String uuidOfDependent = inboundCreatedDependencies.get(uuid);
                conflicts.add(new ConflictMessage(ConflictCase.REMOVAL_OF_DEPENDENCY, 
                        uuid, removedObject.getName(),
                        uuidOfDependent, session.getWorkspace().getObjectInTree(uuidOfDependent).getName()));
            } else if (duplicateMoves.contains(uuid)) {
                removedObjects.remove();
            }
            
        }   
        
        
        // ----- Iterate through outbound properties -----
        
        for (String uuid : outboundPropertiesToChange.keys()) {            
            SPObject changedObject = session.getWorkspace().getObjectInTree(uuid);            
            
            // If this object is being newly added, the rest of the loop body does not matter.
            if (outboundObjectsToAdd.containsKey(uuid)) continue;
            
            // Cannot change a property on an object that no longer exists (due to inbound removal).
            if (changedObject == null) {
                conflicts.add(new ConflictMessage(ConflictCase.CHANGE_OF_REMOVED, uuid, uuid));
                continue;
            }
            
            // Cannot change the property of an object whose direct parent was also changed.
            if (changedObject.getParent() != null && 
                    inboundChangedObjects.contains(changedObject.getParent().getUUID())) {
                conflicts.add(new ConflictMessage(ConflictCase.CHANGE_UNDER_CHANGE, 
                        uuid, changedObject.getName(),
                        changedObject.getParent().getUUID(), changedObject.getParent().getName()));
            }
            
            // You cannot change the property of an object that had a property already changed,
            // unless any and all property changes are identical, in which case the duplicate
            // property changes will be removed from the outgoing list.
                        
            if (inboundChangedObjects.contains(uuid)) {                
                ConflictMessage message = new ConflictMessage(ConflictCase.SIMULTANEOUS_OBJECT_CHANGE, 
                        uuid, session.getWorkspace().getObjectInTree(uuid).getName());
                
                HashMap<String, Object> inboundPropertiesMap = 
                    new HashMap<String, Object>();                
                for (PersistedSPOProperty p : inboundPropertiesToChange.get(uuid)) {
                    inboundPropertiesMap.put(p.getPropertyName(), p.getNewValue());
                }
                                
                Iterator<PersistedSPOProperty> properties = outboundPropertiesToChange.get(uuid).iterator();                
                while (properties.hasNext()) {
                    PersistedSPOProperty p = properties.next();
                    // Check if there is a corresponding inbound property.
                    // If not, this is a conflict since there are non-identical properties.
                    if (inboundPropertiesMap.containsKey(p.getPropertyName())) {
                        if (inboundPropertiesMap.get(p.getPropertyName()).equals(p.getNewValue())) {
                            properties.remove();
                        } else {
                            conflicts.add(message);
                            break;
                        }
                    } else {
                        conflicts.add(message);
                        break;
                    }
                }
            }
            
            // Cannot change the property of a parent whose direct child was either:
            for (SPObject child : changedObject.getChildren()) {                                        
                // also changed
                if (inboundChangedObjects.contains(child.getUUID())) {
                    conflicts.add(new ConflictMessage(ConflictCase.CHANGE_UNDER_CHANGE,
                            uuid, changedObject.getName(),
                            child.getUUID(), child.getName()));                    
                }
                
                // or just added (moved is okay, though).
                if (inboundObjectsToAdd.containsKey(child.getUUID()) &&
                        !inboundObjectsToRemove.containsKey(child.getUUID())){
                    conflicts.add(new ConflictMessage(ConflictCase.CHANGE_AFTER_ADDITION,
                            uuid, changedObject.getName(),
                            child.getUUID(), child.getName()));
                }
            }
        }
        return conflicts;
    }
    
    private String getPersistedObjectName(PersistedSPObject o) {
        for (PersistedSPOProperty p : outboundPropertiesToChange.get(o.getUUID())) {
            if (p.getPropertyName().equals("name")) {
                return (String) p.getNewValue();
            }
        }
        throw new IllegalArgumentException("Given persisted object has no corresponding name property!");
    }

    /**
     * A class that will take a ConflictCase and parameters insert
     * them into the ConflictCase's message.
     */
    private class ConflictMessage {
        
        private final ConflictCase conflict;
        private final String message;
        private final List<String> objectIds = new LinkedList<String>();
        private final List<String> objectNames = new LinkedList<String>();
        
        /**
         * Create a conflict message using the ConflictCase's message
         * and String.format() to put the given arguments in
         * @param conflict
         * @param uuidsAndNames A list of the relevant object ids and names, in pairs.
         * ie: "id1", "table1", "id2", "table2"
         */
        public ConflictMessage(ConflictCase conflict, String ... uuidsAndNames) {
            this.conflict = conflict;
            for (int i = 0; i < uuidsAndNames.length; i += 2) {
                objectIds.add(uuidsAndNames[i]);
                objectNames.add(uuidsAndNames[i+1]);
            }
            if (objectIds.size() != conflict.numArgs()) {
                throw new IllegalArgumentException(
                    "Number of arguments passed in does not match number requested by conflict type");
            }
            try {
                message = String.format(conflict.message, objectNames.toArray());
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        
        /**
         * This constructor is used for custom messages.
         * Will not call String.format().
         * @param message
         * @param conflict
         * @param uuids
         */
        public ConflictMessage(String message, ConflictCase conflict, String ... uuids) {            
            this.message = message;
            this.conflict = conflict;
            objectIds.addAll(Arrays.asList(uuids));
        }
        
        public String getObjectId(int index) {
            return objectIds.get(index);
        }
        
        public ConflictCase getConflictCase() {
            return conflict;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String toString() {
            return message;
        }
        
    }
    
    /**
     * Defines conflict cases as well as messages for each.
     */
    private static enum ConflictCase {
        NO_CONFLICT ("There were no conflicts"),
        
        ADDITION_UNDER_REMOVAL ("The %s you tried adding could not be added because its ancestor was removed"),
        
        MOVE_OF_REMOVED ("Could not move %s because it was removed"),
        
        CHANGE_OF_REMOVED ("Could not change %s because it was removed"),
        
        SIMULTANEOUS_ADDITION ("Could not add %s because a sibling was added/removed"),
        
        ADDITION_UNDER_CHANGE ("Could not add %s because its parent %s was modified"),
        
        CHANGE_AFTER_ADDITION ("Could not change %s because child %s was added"),       
        
        CHANGE_UNDER_CHANGE ("Could not change %s because its parent/child %s was modified"),        
        
        REMOVAL_OF_DEPENDENCY ("Could not remove %s because another object %s is now dependent on it"),
        
        SIMULTANEOUS_OBJECT_CHANGE ("Could not change %s because it was changed by another user"),
        
        DIFFERENT_MOVE ("Could not move %s because it was moved somewhere else"),
        
        SPECIAL_CASE ("");
        
        private final String message;
        
        ConflictCase(String s) {
            message = s;
        }
        
        /**
         * Returns the number of parameters the enum expects in its message.
         */
        public int numArgs() {
            return message.length() - message.replace("%s", "1").length();
        }
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
                    projectLocation.getServiceInfo().getPath() + 
                    "/" + ArchitectClientSideSession.REST_TAG + "/project/" + projectLocation.getUUID(), 
                    "currentRevision=" + currentRevision, null);
            HttpPost postRequest = new HttpPost(serverURI);
            postRequest.setEntity(new StringEntity(jsonArray)); 
            postRequest.setHeader("Content-Type", "application/json");
            HttpUriRequest request = postRequest;
            return outboundHttpClient.execute(request, new JSONResponseHandler());
        } catch (AccessDeniedException ade) {
            throw new AccessDeniedException("Access Denied");
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
        } catch (AccessDeniedException ade) {
            throw new AccessDeniedException("Access Denied");
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
        /**
         * Fired when an update from the server has been performed on the client
         * @param resolver The NetworkConflictResolver that received the update 
         * @return true if the listener should be removed from
         * listener list and should not receive any more calls
         */
        public boolean updatePerformed(NetworkConflictResolver resolver);
        public boolean updateException(NetworkConflictResolver resolver);
        
        /**
         * Notifies listeners that the workspace was deleted.
         * Swing sessions should listen for this to disable the enterprise session.
         * The listener is removed after this method is called.
         */
        public void workspaceDeleted();

        /**
         * Called just before an update will be performed by the
         * {@link NetworkConflictResolver}. This gives objects the chance to be
         * aware of incoming changes from the server if necessary.
         * 
         * @param resolver
         *            The {@link NetworkConflictResolver} that received the
         *            update.
         */
        public void preUpdatePerformed(NetworkConflictResolver resolver);
    }
}
