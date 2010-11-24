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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.security.AccessDeniedException;

import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.dao.FriendlyRuntimeSPPersistenceException;
import ca.sqlpower.dao.MessageSender;
import ca.sqlpower.dao.PersistedSPOProperty;
import ca.sqlpower.dao.PersistedSPObject;
import ca.sqlpower.dao.RemovedObjectEntry;
import ca.sqlpower.dao.SPSessionPersister;
import ca.sqlpower.dao.SPPersister.DataType;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.enterprise.AbstractNetworkConflictResolver;
import ca.sqlpower.enterprise.JSONMessage;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.util.MonitorableImpl;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

import com.enterprisedt.util.debug.Logger;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class ArchitectNetworkConflictResolver extends AbstractNetworkConflictResolver implements MessageSender<JSONObject> {
    

    private static final Logger logger = Logger.getLogger(ArchitectNetworkConflictResolver.class);

    /**
     * Implement a listener of this type to know when a transaction is about to
     * be sent to the server or when a transaction just completed being sent to
     * the server regardless of a success or failure.
     */
    public static interface PostTransactionListener {
        
        public void preServerSend();
        
        public void postServerSend();
    }
    
    private ArchitectClientSideSession session;
    
    private final List<PostTransactionListener> postTransactionListeners = new ArrayList<PostTransactionListener>();
    
    public ArchitectNetworkConflictResolver(
            ProjectLocation projectLocation, 
            SPJSONMessageDecoder jsonDecoder, 
            HttpClient inboundHttpClient, 
            HttpClient outboundHttpClient,
            ArchitectClientSideSession session) 
    {
        super(projectLocation, jsonDecoder, inboundHttpClient, outboundHttpClient, session);
        
        this.session = session;
    }
    
    @Override
    protected void flush(boolean reflush) {
        if (postingJSON.get() && !reflush) {
            return;
        }
        MonitorableImpl monitor = null;
        long startTimeMillis = System.currentTimeMillis();
        long messageLength = messageBuffer.length();
        try {
            postingJSON.set(true);
            
            // Start a progress bar to update the user with the current changes.
            if (session.getStatusInformation() != null) {
                monitor = session.getStatusInformation().createProgressMonitor();
                monitor.setJobSize(messageBuffer.length() + 2);
                monitor.setMessage("Saving");
                monitor.setProgress(0);
                final MonitorableImpl finalMonitor = monitor;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (finalMonitor.getProgress() < finalMonitor.getJobSize()) {
                            try {
                                Thread.sleep((long) currentWaitPerPersist);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            if (finalMonitor.isCancelled() || finalMonitor.isFinished()) break;
                            finalMonitor.incrementProgress();
                        }
                        finalMonitor.setMessage("Completing server update.");
                    }
                }).start();
            }
            
            // Try to send json message ...
            JSONMessage response = null;
            try {
                for (PostTransactionListener l : postTransactionListeners) {
                    l.preServerSend();
                }
                response = postJsonArray(messageBuffer.toString());
                for (PostTransactionListener l : postTransactionListeners) {
                    l.postServerSend();
                }
            } catch (AccessDeniedException e) {
                List<UpdateListener> listenersToRemove = new ArrayList<UpdateListener>();
                for (UpdateListener listener : updateListeners) {
                    if (listener.updateException(ArchitectNetworkConflictResolver.this, e)) {
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
                    throw e;
                }
                return;
            }
            if (response.isSuccessful()) {
                // Sent json message without conflict.
                try {
                    JSONObject jsonObj= new JSONObject(response.getBody());
                    currentRevision = jsonObj.getInt("currentRevision");
                    serverTimestamp = jsonObj.getLong("serverTimestamp");
                    if (logger.isDebugEnabled())
                        logger.debug("Setting currentRevision to: " + currentRevision + 
                                    " and serverTimestamp to: " + serverTimestamp);
                } catch (JSONException e) {
                    throw new RuntimeException("Could not update current revision" + e.getMessage());
                }
                long endTime = System.currentTimeMillis();
                if (messageLength != 0) {
                    double processTimePerObj = ((double)(endTime - startTimeMillis)) / (double)messageLength;
                    currentWaitPerPersist = currentWaitPerPersist * 0.9 + processTimePerObj * 0.1;
                }
            } else {
                
                if (response.getStatusCode() == 403) { // FORBIDDEN, server timestamp is newer
                    updateListeners.clear();
                    if (projectLocation.getUUID().equals("system")) {
                        if (upf != null) {
                            upf.createUserPrompter("Server at " + projectLocation.getServiceInfo().getServerAddress() + "has failed since your session began." +
                                 " Please restart the program to synchronize the system workspace with the server." , 
                                UserPromptType.MESSAGE, 
                                UserPromptOptions.OK, 
                                UserPromptResponse.OK, 
                                null, "OK").promptUser();  
                        }
                    } else {
                        if (upf != null) {
                            upf.createUserPrompter("Server at "  + projectLocation.getServiceInfo().getServerAddress() + 
                                " has failed since your session began." +
                                " Please use the refresh button to synchronize workspace " + projectLocation.getName() + 
                                " with the server.", 
                                UserPromptType.MESSAGE, 
                                UserPromptOptions.OK, 
                                UserPromptResponse.OK, 
                                null, "OK").promptUser();
                        }
                    }
                    return;
                }
                // Did not successfully post json, we must update ourselves, and then try again if we can.
                if (!reflush) {
                    // These lists should reflect the state of the workspace at the time of the conflict.
                    // The workspace could be updated several times before a successful post is made.
                    fillOutboundPersistedLists();
                }
                // Try to rollback our changes
                try {
                    SPSessionPersister.undoForSession(session.getWorkspace(), 
                            new LinkedList<PersistedSPObject>(outboundObjectsToAdd.values()),
                            LinkedListMultimap.create(outboundPropertiesToChange), 
                            new LinkedList<RemovedObjectEntry>(outboundObjectsToRemove.values()), converter);
                } catch (Exception e) {
                    throw new RuntimeException("Reflush failed on rollback", e);
                }
                
                //If the preconditions failed which caused the persist to fail don't try to 
                //push the persist forward again.
                if (!response.isSuccessful() && response.getStatusCode() == 412) {
                    logger.info("Friendly error occurred, " + response);
                    throw new FriendlyRuntimeSPPersistenceException(response.getBody());
                }
                
                final String json;
                final int newRev;
                final long timestamp;
                try {
                    JSONObject jsonObject = new JSONObject(response.getBody());
                    json = jsonObject.getString("data");
                    newRev = jsonObject.getInt("currentRevision");
                    timestamp = jsonObject.getLong("serverTimestamp");
                } catch (Exception e) {
                    throw new RuntimeException("Reflush failed on getJson", e);
                }
                // Try to create inboundPersistedLists for comparison with the outbound. These will be used
                // for special case collision detection.
                fillInboundPersistedLists(json);

                // Try to apply update
                decodeMessage(new JSONTokener(json), newRev, timestamp);
                // We need an additional step here for checking for special case conflicts
                List<ConflictMessage> conflicts = detectConflicts();
                if (conflicts.size() == 0) {
                    // Try to return the persisted objects to their state pre-update.
                    try {
                        listener.clear();
                        for (Map.Entry<String, PersistedSPObject> entry : outboundObjectsToAdd.entrySet()) {
                            entry.getValue().setLoaded(false);
                        }
                        SPSessionPersister.redoForSession(getWorkspace(), 
                                new LinkedList<PersistedSPObject>(outboundObjectsToAdd.values()),
                                LinkedListMultimap.create(outboundPropertiesToChange), 
                                new LinkedList<RemovedObjectEntry>(outboundObjectsToRemove.values()), converter);
                        // We want to re-send our changes, but only if we were able to restore them
                        flush(true);
                    } catch (Exception ex) {
                        throw new RuntimeException("Reflush failed on rollforward", ex);
                    }
                } else {
                    String message = "";
                    StringBuilder sb = new StringBuilder();
                    message += "Your changes have been discarded due to a conflict between you and another user: \n";
                    for (int i = 0; i < AbstractNetworkConflictResolver.MAX_CONFLICTS_TO_DISPLAY && i < conflicts.size(); i++) {
                        sb.append(conflicts.get(i).getMessage() + "\n");
                    }
                    message = sb.toString();
                    session.createUserPrompter(message, 
                            UserPromptType.MESSAGE, 
                            UserPromptOptions.OK, 
                            UserPromptResponse.OK, 
                            "OK", "OK").promptUser("");
                }
            }
        } finally {
            if (monitor != null) {
                monitor.setFinished(true);
            }
            postingJSON.set(false);
            clear(true);
        }
    }
    
    @Override
    protected List<ConflictMessage> detectConflicts() {
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
            Class<?> type;
            String parentId;       
            SPObject spo = session.getWorkspace().getObjectInTree(uuid);
            PersistedSPObject o = outboundObjectsToAdd.get(uuid);
            try {
                if (spo != null) {
                    type = spo.getClass();
                    parentId = spo.getParent().getUUID();
                } else if (o != null) {                    
                    type = Class.forName(o.getType(), true, ArchitectNetworkConflictResolver.class.getClassLoader());
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
                        Class<PlayPenComponent> type = (Class<PlayPenComponent>) Class.forName(o.getType(), true, ArchitectNetworkConflictResolver.class.getClassLoader());
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

    @Override
    protected SPObject getWorkspace() {
        return session.getWorkspace();
    }
    
    public void addPostTransactionListener(PostTransactionListener l) {
        postTransactionListeners.add(l);
    }
    
    public void removePostTransactionListener(PostTransactionListener l) {
        postTransactionListeners.remove(l);
    }
}
