package ca.sqlpower.architect.enterprise;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.AccessDeniedException;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.SPObjectSnapshotHierarchyListener;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersisterListener;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.dao.json.SPJSONPersister;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.diff.DiffInfo;
import ca.sqlpower.diff.SimpleDiffChunkJSONConverter;
import ca.sqlpower.enterprise.TransactionInformation;
import ca.sqlpower.enterprise.client.RevisionController;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.object.SPObjectUUIDComparator;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.sqlobject.UserDefinedSQLTypeSnapshot;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;

public class ArchitectClientSideSession extends ArchitectSessionImpl implements RevisionController {	
	
	private static Logger logger = Logger.getLogger(ArchitectClientSideSession.class);
	private static CookieStore cookieStore = new BasicCookieStore();
	
	public static final String MONDRIAN_SCHEMA_REL_PATH = "/mondrian";

    /**
     * All requests to the server will contain this tag after the enterprise
     * server name (which is normally architect-enterprise).
     */
	static final String REST_TAG = "rest";
	
	/**
	 * The prefs node that will store information about the current settings of
	 * the DDL generator and compare DM panels. Currently this is stored in prefs
	 * because we want to store it per user for each project they are using. In the
	 * future we may want to store this in the server, once per user per project.
	 */
	private final Preferences prefs = Preferences.userNodeForPackage(ArchitectClientSideSession.class);
	
	/**
	 * Describes the location of the project that this session represents.
	 */
	private final ProjectLocation projectLocation;

    /**
     * An {@link HttpClient} used to send updates to the server for changes to
     * the project and to receive updates from other users from the server.
     */
	private final HttpClient outboundHttpClient;
	
	/**
	 * The persister that will update the project in this session with changes from
	 * the server.
	 */
	private final ArchitectSessionPersister sessionPersister;
	
	/**
	 * Used to convert JSON sent from the server into persist calls to forward the
	 * server changes to the {@link #sessionPersister}.
	 */
	private final SPJSONPersister jsonPersister;
	private final NetworkConflictResolver updater;
	private final SPJSONMessageDecoder jsonMessageDecoder;
	private final DataSourceCollectionUpdater dataSourceCollectionUpdater = new DataSourceCollectionUpdater();
	
	private DataSourceCollection <JDBCDataSource> dataSourceCollection;

	/**
	 * Used to store sessions which hold nothing but security info.
	 */
	public static Map<String, ArchitectClientSideSession> securitySessions;
    static {
        securitySessions = new HashMap<String, ArchitectClientSideSession>();
    }
	
	public ArchitectClientSideSession(ArchitectSessionContext context, 
			String name, ProjectLocation projectLocation) throws SQLObjectException {
		super(context, name, new ArchitectSwingProject());
		
		setupSnapshots();
		
		this.projectLocation = projectLocation;
		this.isEnterpriseSession = true;
		
		String ddlgClass = prefs.get(this.projectLocation.getUUID() + ".ddlg", null);
		if (ddlgClass != null) {
		    try {
                DDLGenerator ddlg = (DDLGenerator) Class.forName(ddlgClass, true, ArchitectClientSideSession.class.getClassLoader()).newInstance();
                setDDLGenerator(ddlg);
                ddlg.setTargetCatalog(prefs.get(this.projectLocation.getUUID() + ".targetCatalog", null));
                ddlg.setTargetSchema(prefs.get(this.projectLocation.getUUID() + ".targetSchema", null));
            } catch (Exception e) {
                createUserPrompter("Cannot load DDL settings due to missing class " + ddlgClass, 
                        UserPromptType.MESSAGE, UserPromptOptions.OK, UserPromptResponse.OK, null, "OK");
                logger.error("Cannot find DDL Generator for class " + ddlgClass + 
                        ", ddl generator properties are not loaded.");
            }
		}
		
		outboundHttpClient = createHttpClient(projectLocation.getServiceInfo());
		dataSourceCollection = getDataSources();
		
		sessionPersister = new ArchitectSessionPersister("inbound-" + projectLocation.getUUID(), getWorkspace(), 
		        new ArchitectPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		sessionPersister.setWorkspaceContainer(this);
		
		jsonMessageDecoder = new SPJSONMessageDecoder(sessionPersister);
		
		updater = new NetworkConflictResolver(
		        projectLocation, 
		        jsonMessageDecoder, 
		        createHttpClient(projectLocation.getServiceInfo()), 
		        outboundHttpClient, this);
		
		jsonPersister = new SPJSONPersister(updater);
		
		try {
            ServerInfoProvider.getServerVersion(
                    projectLocation.getServiceInfo().getServerAddress(), 
                    String.valueOf(projectLocation.getServiceInfo().getPort()), 
                    projectLocation.getServiceInfo().getPath(), 
                    projectLocation.getServiceInfo().getUsername(), 
                    projectLocation.getServiceInfo().getPassword());
        } catch (Exception e) {
            throw new AssertionError("Exception encountered while verifying the server license:" + e.getMessage());
        }
	}

    /**
     * Helper method for the constructor of a client side session.
     * <p>
     * This method will attach listeners and update snapshots as necessary on
     * the project's load.
     */
    private void setupSnapshots() {
        getTargetDatabase().addSPListener(new SPObjectSnapshotHierarchyListener(this));
        
        //This listener will update the obsolete flag on snapshots being added
        //to the workspace to keep old snapshot's obsolete flag correct when
        //they haven't been opened in some time but updates have occurred to the types.
        //While this will also check new snapshots being added to the system doing
        //the check doesn't hurt anything.
        getWorkspace().addSPListener(new AbstractPoolingSPListener() {
            @Override
            public void childAddedImpl(SPChildEvent e) {
                if (e.getChild() instanceof UserDefinedSQLTypeSnapshot) {
                    UserDefinedSQLTypeSnapshot snapshot = (UserDefinedSQLTypeSnapshot) e.getChild();
                    if (!snapshot.isObsolete()) {
                        UserDefinedSQLType systemType = findSystemTypeFromSnapshot(snapshot);
                        if (!UserDefinedSQLType.areEqual(snapshot.getSPObject(), systemType)) {
                            snapshot.setObsolete(true);
                        }
                    }
                }
            }
        });
    }

	// -
	
	public static Map<String, ArchitectClientSideSession> getSecuritySessions() {
        return securitySessions;
    }
	
	@Override
    public boolean close() {
    	if (getDDLGenerator() != null) {
    	    if (getDDLGenerator().getTargetCatalog() != null) {
    	        prefs.put(projectLocation.getUUID() + ".targetCatalog", getDDLGenerator().getTargetCatalog());
    	    }
    	    if (getDDLGenerator().getTargetSchema() != null) {
    	        prefs.put(projectLocation.getUUID() + ".targetSchema", getDDLGenerator().getTargetSchema());
    	    }
    	    prefs.put(projectLocation.getUUID() + ".ddlg", getDDLGenerator().getClass().getName());
    	}
    	
    	try {
    	    //TODO: Figure out how to de-register the session &c.
		} catch (Exception e) {
			try {
				logger.error(e);
				
				createUserPrompter("Cannot access the server to close the server session", 
						UserPromptType.MESSAGE,
						UserPromptOptions.OK, 
						UserPromptResponse.OK, 
						UserPromptResponse.OK, "OK");
				
			} catch (Throwable t) {
				//do nothing here because we failed on logging the error.
			}
		}
		
		updater.interrupt();
        outboundHttpClient.getConnectionManager().shutdown();
        
        if (dataSourceCollection != null) {
            dataSourceCollectionUpdater.detach(dataSourceCollection);
        }
        
        return super.close();
    }
	
	@Override
	public DataSourceCollection<JDBCDataSource> getDataSources() {
		if (dataSourceCollection != null) {
			return dataSourceCollection;
		}
		
		ResponseHandler<DataSourceCollection<JDBCDataSource>> plIniHandler = 
            new ResponseHandler<DataSourceCollection<JDBCDataSource>>() {
            public DataSourceCollection<JDBCDataSource> handleResponse(HttpResponse response)
                    throws ClientProtocolException, IOException {
                
                if (response.getStatusLine().getStatusCode() == 401) {
                    throw new AccessDeniedException("Access Denied");
                }

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException(
                            "Server error while reading data sources: " + response.getStatusLine());
                }
                
                
                PlDotIni plIni;
                try {
					plIni = new PlDotIni(getServerURI(projectLocation.getServiceInfo(), "/" + REST_TAG +"/jdbc/"),
                            getServerURI(projectLocation.getServiceInfo(), MONDRIAN_SCHEMA_REL_PATH)) {
					    
					    @Override
					    public List<UserDefinedSQLType> getSQLTypes() {
					        List<UserDefinedSQLType> types = new ArrayList<UserDefinedSQLType>();
					        types.addAll(ArchitectClientSideSession.this.getSQLTypes());
					        for (DomainCategory dc : ArchitectClientSideSession.this.getDomainCategories()) {
					            types.addAll(dc.getChildren(UserDefinedSQLType.class));
					        }
					        return types;
					    }
					    
					    @Override
					    public UserDefinedSQLType getNewSQLType(String name, int jdbcCode) {
					        UserDefinedSQLType newType = new UserDefinedSQLType();
					        newType.setName(name);
					        newType.setType(jdbcCode);
					        ArchitectSwingProject systemWorkspace = ArchitectClientSideSession.this.getSystemWorkspace();
                            systemWorkspace.addChild(newType, systemWorkspace.getChildren(UserDefinedSQLType.class).size());
                            return newType;
					    }
					};
                    plIni.read(response.getEntity().getContent());
                    logger.debug("Data source collection has URI " + plIni.getServerBaseURI());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                
                return new SpecificDataSourceCollection<JDBCDataSource>(plIni, JDBCDataSource.class);
            }
        };
        
        try {
            dataSourceCollection = executeServerRequest(outboundHttpClient, projectLocation.getServiceInfo(), 
                    "/" + REST_TAG + "/data-sources/", plIniHandler);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        dataSourceCollectionUpdater.attach(dataSourceCollection);
        
        return dataSourceCollection;
	}
	
	public void startUpdaterThread() {
		
		final SPPersisterListener listener = new SPPersisterListener(jsonPersister, sessionPersister,
						new ArchitectPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		SQLPowerUtils.listenToHierarchy(getWorkspace(), listener);
		
		updater.setListener(listener);
		updater.setConverter(new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		updater.start();
		
		addSessionLifecycleListener(new SessionLifecycleListener<ArchitectSession>() {
			public void sessionClosing(SessionLifecycleEvent<ArchitectSession> e) {
				SQLPowerUtils.unlistenToHierarchy(getWorkspace(), listener);
			}

            public void sessionOpening(SessionLifecycleEvent<ArchitectSession> e) {
            }
		});
	}
	
	public User getUser() {
	    String username = getProjectLocation().getServiceInfo().getUsername();
	    User currentUser = null;
        for (User user : getSystemWorkspace().getChildren(User.class)) {
            if (user.getUsername().equals(username)) {
                currentUser = user;
            }
        }
	    return currentUser;
	}
	
	public void persistProjectToServer() throws SPPersistenceException {
		final SPPersisterListener tempListener = new SPPersisterListener(jsonPersister,
						new ArchitectPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		tempListener.persistObject(getWorkspace(), 0);
	}
	
	public ArchitectSwingProject getSystemWorkspace() {
		return getSecuritySessions().get(getProjectLocation().getServiceInfo().getServerAddress()).getWorkspace();
	}
	
	public ArchitectClientSideSession getSystemSession() {
	    return getSecuritySessions().get(getProjectLocation().getServiceInfo().getServerAddress());
	}

	@Override
	public void runInForeground(Runnable runner) {
		// If we're in a SwingContext, run on the Swing Event Dispatch thread.
		// XXX: This is a bit of a quickfix and I think a better way to possibly fix
		// this could be to have WabitServerSession implement WabitSession, and
		// use a delegate session to delegate most of the server calls (instead
		// of extending WabitSessionImpl). Then if it's in a swing context, it would
		// have a WabitSwingSession instead.
		if (getContext() instanceof ArchitectSwingSessionContext) {
			SwingUtilities.invokeLater(runner);
		} else {
			super.runInForeground(runner);
		}
	}
	
	/**
	 * Exposes the shared cookie store so we don't spawn useless sessions
	 * through the client.
	 */
	public static CookieStore getCookieStore() {
		return cookieStore;
	}
	
	public ProjectLocation getProjectLocation() {
		return projectLocation;
	}
	
	// -
	
	public static List<ProjectLocation> getWorkspaceNames(SPServerInfo serviceInfo) 
	throws IOException, URISyntaxException, JSONException {
    	HttpClient httpClient = createHttpClient(serviceInfo);
    	try {
    		HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, "/" + REST_TAG + "/jcr/projects"));
    		JSONMessage message = httpClient.execute(request, new JSONResponseHandler());
    		List<ProjectLocation> workspaces = new ArrayList<ProjectLocation>();
    		JSONArray response = new JSONArray(message.getBody());
    		for (int i = 0; i < response.length(); i++) {
    			JSONObject workspace = (JSONObject) response.get(i);
    			workspaces.add(new ProjectLocation(
    					workspace.getString("uuid"),
    					workspace.getString("name"),
    					serviceInfo));
    		}
    		return workspaces;
    	} catch (AccessDeniedException e) {
    	    throw e;
    	} finally {
    		httpClient.getConnectionManager().shutdown();
    	}
    }
	
    public List<TransactionInformation> getTransactionList(long fromVersion, long toVersion)
    throws IOException, URISyntaxException, JSONException, ParseException {
        
        SPServerInfo serviceInfo = projectLocation.getServiceInfo();
        HttpClient httpClient = createHttpClient(serviceInfo);
        
        try {
            
            logger.info("Getting transactions between " + fromVersion + " and " + toVersion);
            JSONMessage message = executeServerRequest(httpClient, projectLocation.getServiceInfo(),
                    "/" + REST_TAG + "/project/" + projectLocation.getUUID() + "/revision_list",
                    "versions=" + fromVersion + ":" + toVersion,
                    new JSONResponseHandler());
            
            return decodeJSONRevisionList(message.getBody());
            
        } finally {
            httpClient.getConnectionManager().shutdown();
        }               
        
    }
    
    public static ProjectLocation createNewServerSession(SPServerInfo serviceInfo, String name, ArchitectSession session)
    throws URISyntaxException, ClientProtocolException, IOException, JSONException {
        
    	HttpClient httpClient = createHttpClient(serviceInfo);
    	try {
    		HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, "/" + REST_TAG + "/jcr/projects/new", "name=" + name));
    		JSONMessage message = httpClient.execute(request, new JSONResponseHandler());
    		JSONObject response = new JSONObject(message.getBody());
    		return new ProjectLocation(
    					response.getString("uuid"),
    					response.getString("name"),
    					serviceInfo);
    	} catch (AccessDeniedException e) {
    	    session.createUserPrompter("You do not have sufficient privileges to create a new workspace.", 
                       UserPromptType.MESSAGE, 
                       UserPromptOptions.OK, 
                       UserPromptResponse.OK, 
                       "OK", "OK").promptUser("");
    	    return null;
    	} finally {
    		httpClient.getConnectionManager().shutdown();
    	}
    }
	
	public int revertServerWorkspace(int revisionNo) throws IOException, URISyntaxException, JSONException {
	    return revertServerWorkspace(projectLocation, revisionNo);
	}
	
	/**
	 * This method reverts the server workspace specified by the given project location
	 * to the specified revision number.
	 * 
	 * All sessions should automatically update to the reverted revision due to their Updater.
	 * 
	 * @returns The new global revision number, right after the reversion, or -1 if the server did not revert.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws JSONException 
	 */
	public static int revertServerWorkspace(ProjectLocation projectLocation, int revisionNo)
	throws IOException, URISyntaxException, JSONException {
        SPServerInfo serviceInfo = projectLocation.getServiceInfo();
        HttpClient httpClient = createHttpClient(serviceInfo);
        
        try {
            JSONMessage message = executeServerRequest(httpClient, projectLocation.getServiceInfo(),
                    "/" + REST_TAG + "/project/" + projectLocation.getUUID() + "/revert",
                    "revisionNo=" + revisionNo, 
                    new JSONResponseHandler());    
            if (message.isSuccessful()) {
                return new JSONObject(message.getBody()).getInt("currentRevision");
            } else {
                return -1;
            }
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        
	}

    /**
     * This method can update any users password on the server given the correct
     * old password and done by a user with the privileges to change the user's
     * password.
     * 
     * @param session
     *            The client session that has the correct server information to
     *            post requests to the server.
     * @param username
     *            The user name of the user to update.
     * @param oldPassword
     *            The old password of the user to validate that the password can
     *            be updated correctly.
     * @param newPassword
     *            The new password to update to.
     * @param upf
     *            A user prompter to display message and error information to
     *            the user as necessary.
     */
	public static void updateUserPassword(ArchitectClientSideSession session, String username, 
	        String oldPassword, String newPassword, UserPrompterFactory upf) {
	    SPServerInfo serviceInfo = session.getProjectLocation().getServiceInfo();
        
        HttpClient client = ArchitectClientSideSession.createHttpClient(serviceInfo);
        
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("oldPassword", new String(Hex.encodeHex(digester.digest(oldPassword.getBytes()))));
            json.put("newPassword", new String(Hex.encodeHex(digester.digest(newPassword.getBytes()))));
            
            URI serverURI = new URI("http", null, 
                    serviceInfo.getServerAddress(), 
                    serviceInfo.getPort(),
                    serviceInfo.getPath() + "/" + REST_TAG + "/project/system/change_password", 
                    null, null);
            HttpPost postRequest = new HttpPost(serverURI);
            postRequest.setEntity(new StringEntity(json.toString())); 
            postRequest.setHeader("Content-Type", "application/json");
            HttpUriRequest request = postRequest;
            client.execute(request, new JSONResponseHandler());
        } catch (AccessDeniedException ex) {
            logger.warn("Failed password change", ex);
            upf.createUserPrompter("The password you have entered is incorrect.", 
                    UserPromptType.MESSAGE, 
                    UserPromptOptions.OK, 
                    UserPromptResponse.OK, 
                    "OK", "OK").promptUser("");
        } catch (Exception ex) {
            logger.warn("Failed password change", ex);
            upf.createUserPrompter(
                    "Could not change the password due to the following: " + 
                    ex.getMessage() + " See logs for more details.", 
                    UserPromptType.MESSAGE, 
                    UserPromptOptions.OK, 
                    UserPromptResponse.OK, 
                    "OK", "OK").promptUser("");
        }
	}
	
	public void persistRevisionFromServer(int revisionNo, SPJSONMessageDecoder targetDecoder)
	throws IOException, URISyntaxException, SPPersistenceException, IllegalArgumentException {
	    ArchitectClientSideSession.persistRevisionFromServer(projectLocation, revisionNo, targetDecoder);
	}
	
	/**
	 * Requests the server for persist calls from version 0 to the given revision
	 * of the given project, and persists them to the given decoder.
	 * 
	 * @param projectLocation
	 * @param revisionNo Must be greater than zero, and no greater than the current revision number
	 * @param decoder
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SPPersistenceException
	 * @throws IllegalArgumentException Thrown if the server rejects the given revisionNo
	 */
	public static void persistRevisionFromServer(ProjectLocation projectLocation, 
	        int revisionNo, SPJSONMessageDecoder decoder)
	throws IOException, URISyntaxException, SPPersistenceException, IllegalArgumentException {
	    
	    SPServerInfo serviceInfo = projectLocation.getServiceInfo();
	    HttpClient httpClient = createHttpClient(serviceInfo);
        
        try {
            JSONMessage response = executeServerRequest(httpClient, serviceInfo,
                    "/" + REST_TAG + "/project/" + projectLocation.getUUID() + "/" + revisionNo,
                    new JSONResponseHandler());            
            
            if (response.isSuccessful()) {
                decoder.decode(response.getBody());                
            } else {
                throw new IllegalArgumentException("The server rejected the revision number " +
                		"(it must be greater than 0, and no greater than the current revision number)");
            }
            
        } finally {
            httpClient.getConnectionManager().shutdown();
        }   
	}

	/**
     * Gets a list of DiffChunks representing the differences between the two revisions from the server.
     */
	public List<DiffChunk<DiffInfo>> getComparisonDiffChunks(int oldRevisionNo, int newRevisionNo) 
	throws IOException, URISyntaxException, JSONException, SPPersistenceException {
	    
        SPServerInfo serviceInfo = projectLocation.getServiceInfo();
        HttpClient httpClient = createHttpClient(serviceInfo);
        
        try {
            JSONMessage response = executeServerRequest(httpClient, projectLocation.getServiceInfo(),
                    "/" + REST_TAG + "/project/" + projectLocation.getUUID() + "/compare",
                    "versions=" + oldRevisionNo + ":" + newRevisionNo, 
                    new JSONResponseHandler());    
                                  
            return SimpleDiffChunkJSONConverter.decode(response.getBody());
            
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        
	}
	
	public static List<TransactionInformation> decodeJSONRevisionList(String json) 
	throws JSONException, ParseException {
        JSONArray jsonArray = new JSONArray(json);
        List<TransactionInformation> transactions = new ArrayList<TransactionInformation>();
        
        for (int i = 0; i < jsonArray.length(); i++) {
            
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            TransactionInformation transaction = new TransactionInformation(
                    jsonItem.getLong("number"),                     
                    jsonItem.getLong("time"),
                    jsonItem.getString("author"),
                    jsonItem.getString("description"),
                    jsonItem.getString("simpleDescription"));
            transactions.add(transaction);
            
        }
        
        return transactions;
	}
	
	public static void deleteServerWorkspace(ProjectLocation projectLocation, ArchitectSession session) throws URISyntaxException, ClientProtocolException, IOException {
    	SPServerInfo serviceInfo = projectLocation.getServiceInfo();
    	HttpClient httpClient = createHttpClient(serviceInfo);
    	
    	try {
    		executeServerRequest(httpClient, projectLocation.getServiceInfo(),
    		        "/" + REST_TAG + "/jcr/" + projectLocation.getUUID() + "/delete", 
    				new JSONResponseHandler());
    	} catch (AccessDeniedException e) { 
    	    session.createUserPrompter("You do not have sufficient privileges to delete the selected workspace.", 
                       UserPromptType.MESSAGE, 
                       UserPromptOptions.OK, 
                       UserPromptResponse.OK, 
                       "OK", "OK").promptUser(""); 
    	} finally {
    		httpClient.getConnectionManager().shutdown();
    	}
    }
	
	private static <T> T executeServerRequest(HttpClient httpClient, SPServerInfo serviceInfo, 
            String contextRelativePath, ResponseHandler<T> responseHandler)throws IOException, URISyntaxException {
        return executeServerRequest(httpClient, serviceInfo, contextRelativePath, null, responseHandler);
    }	
	
	private static <T> T executeServerRequest(HttpClient httpClient, SPServerInfo serviceInfo, 
	        String contextRelativePath, String query, ResponseHandler<T> responseHandler) throws IOException, URISyntaxException {
	    HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, contextRelativePath, query));  
	    return httpClient.execute(request, responseHandler);
	}
	
	private static URI getServerURI(SPServerInfo serviceInfo, String contextRelativePath) throws URISyntaxException {
	    return getServerURI(serviceInfo, contextRelativePath, null);
	}
	
	private static URI getServerURI(SPServerInfo serviceInfo, String contextRelativePath, String query) throws URISyntaxException {
        String contextPath = serviceInfo.getPath();
        URI serverURI = new URI("http", null, serviceInfo.getServerAddress(), serviceInfo.getPort(),
                contextPath + contextRelativePath, query, null);
        return serverURI;
    }
	
	public static HttpClient createHttpClient(SPServerInfo serviceInfo) {
		HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 2000);
        DefaultHttpClient httpClient = new DefaultHttpClient(params);
        httpClient.setCookieStore(cookieStore);
        httpClient.getCredentialsProvider().setCredentials(
            new AuthScope(serviceInfo.getServerAddress(), AuthScope.ANY_PORT), 
            new UsernamePasswordCredentials(serviceInfo.getUsername(), serviceInfo.getPassword()));
        return httpClient;
	}
    
    public NetworkConflictResolver getUpdater() {
        return updater;
    }
	
	private class DataSourceCollectionUpdater implements DatabaseListChangeListener, PropertyChangeListener, UndoableEditListener {
    	
    	/**
    	 * If true this updater is currently posting properties to the server. If
    	 * properties are being posted to the server and an event comes in because
    	 * of a change during posting the updater should not try to repost the message
    	 * it is currently trying to post.
    	 */
    	private boolean postingProperties = false;

    	private final ResponseHandler<Void> responseHandler = new ResponseHandler<Void>() {
            public Void handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new ClientProtocolException(
                            "Failed to create/update data source on server. Reason:\n" +
                            EntityUtils.toString(response.getEntity()));
                } else {
                    return null;
                }
            }
        };
    	
        public void attach(DataSourceCollection<JDBCDataSource> dsCollection) {
            dsCollection.addDatabaseListChangeListener(this);
            dsCollection.addUndoableEditListener(this);
            
            for (JDBCDataSourceType jdst : dsCollection.getDataSourceTypes()) {
                jdst.addPropertyChangeListener(this);
            }
            
            for (SPDataSource ds : dsCollection.getConnections()) {
                ds.addPropertyChangeListener(this);
            }
        }
        
        public void detach(DataSourceCollection<JDBCDataSource> dsCollection) {
            dsCollection.removeDatabaseListChangeListener(this);
            dsCollection.removeUndoableEditListener(this);
            
            for (JDBCDataSourceType jdst : dsCollection.getDataSourceTypes()) {
                jdst.removePropertyChangeListener(this);
            }
            
            for (SPDataSource ds : dsCollection.getConnections()) {
                ds.removePropertyChangeListener(this);
            }
        }

        /**
         * Handles the addition of a new database entry, relaying its current
         * state to the server. Also begins listening to the new data source as
         * would have happened if the new data source existed before
         * {@link #attach(DataSourceCollection)} was invoked.
         */
        public void databaseAdded(DatabaseListChangeEvent e) {
            SPDataSource source = e.getDataSource();
            source.addPropertyChangeListener(this);
            
            List<NameValuePair> properties = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> ent : source.getPropertiesMap().entrySet()) {
                properties.add(new BasicNameValuePair(ent.getKey(), ent.getValue()));
            }
            
            if (source instanceof JDBCDataSource) {
                postJDBCDataSourceProperties((JDBCDataSource) source, properties);
            }
            
            if (source instanceof Olap4jDataSource) {
                postOlapDataSourceProperties((Olap4jDataSource) source, properties);
            }
        }

        /**
         * Handles changes to individual data sources by relaying their new
         * state to the server.
         * <p>
         * <b>Implementation note:</b> Presently, all properties for the data
         * source are sent back to the server every time one of them changes.
         * This is not the desired behaviour, but without rethinking the
         * SPDataSource event system, there is little else we can do: the
         * property change events tell us JavaBeans property names, but in order
         * to send incremental updates, we's need to know the pl.ini property
         * key names.
         * 
         * @param evt
         *            The event describing the change. Its source must be the
         *            data source object which was modified.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            // Updating all properties is less than ideal, but a property change event does
            // not tell us what the "pl.ini" key for the property is.

            Object source = evt.getSource();
            
            if (source instanceof SPDataSource) {
                SPDataSource ds = (SPDataSource) source;
                ds.addPropertyChangeListener(this);
                
                List<NameValuePair> properties = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> ent : ds.getPropertiesMap().entrySet()) {
                    properties.add(new BasicNameValuePair(ent.getKey(), ent.getValue()));
                }
                
                if (ds instanceof JDBCDataSource) {
                    postJDBCDataSourceProperties((JDBCDataSource) ds, properties);
                }
                
                if (ds instanceof Olap4jDataSource) {
                    postOlapDataSourceProperties((Olap4jDataSource) ds, properties);
                }
            }
            
            if (source instanceof JDBCDataSourceType) {
                JDBCDataSourceType jdst = (JDBCDataSourceType) source;
                jdst.addPropertyChangeListener(this);
                
                List<NameValuePair> properties = new ArrayList<NameValuePair>();
                for (String name : jdst.getPropertyNames()) {
                    properties.add(new BasicNameValuePair(name, jdst.getProperty(name)));
                }
                
                postJDBCDataSourceTypeProperties(jdst, properties);
            }
        }

        private void postJDBCDataSourceProperties(JDBCDataSource ds,
                List<NameValuePair> properties) {
        	if (postingProperties) return;
        	
            HttpClient httpClient = createHttpClient(projectLocation.getServiceInfo());
            try {
                HttpPost request = new HttpPost(jdbcDataSourceURI(ds));
                request.setEntity(new UrlEncodedFormEntity(properties));
				httpClient.execute(request, responseHandler);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
        
        private void postOlapDataSourceProperties(Olap4jDataSource ods,
                List<NameValuePair> properties) {
            if (postingProperties) return;
            
            HttpClient httpClient = createHttpClient(projectLocation.getServiceInfo());
            try {
                File schemaFile = new File(ods.getMondrianSchema());
                
                if (!schemaFile.exists()) 
                    logger.error("Schema file " + schemaFile.getAbsolutePath() + 
                            " does not exist for data source " + ods.getName());
                
                HttpPost request = new HttpPost(
                        getServerURI(projectLocation.getServiceInfo(), 
                                MONDRIAN_SCHEMA_REL_PATH + schemaFile.getName()));
                
                request.setEntity(new FileEntity(schemaFile, "text/xml"));
                httpClient.execute(request, responseHandler);
                
                //updating new data source to point to the server's schema.
                for (int i = properties.size() - 1; i >= 0; i--) {
                    NameValuePair pair = properties.get(i);
                    if (pair.getName().equals(Olap4jDataSource.MONDRIAN_SCHEMA)) {
                        properties.add(new BasicNameValuePair(
                                Olap4jDataSource.MONDRIAN_SCHEMA, 
                                SPDataSource.SERVER + schemaFile.getName()));
                        properties.remove(pair);
                        break;
                    }
                }
                
                try {
                    postingProperties = true;
                    ods.setMondrianSchema(new URI(SPDataSource.SERVER + schemaFile.getName()));
                } finally {
                    postingProperties = false;
                }
                
                request = new HttpPost(olapDataSourceURI(ods));
                request.setEntity(new UrlEncodedFormEntity(properties));
                httpClient.execute(request, responseHandler);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }

        private void postJDBCDataSourceTypeProperties(JDBCDataSourceType jdst,
                List<NameValuePair> properties) {
            if (postingProperties) return;
            
            HttpClient httpClient = createHttpClient(projectLocation.getServiceInfo());
            try {
                HttpPost request = new HttpPost(jdbcDataSourceTypeURI(jdst));
                request.setEntity(new UrlEncodedFormEntity(properties));
                httpClient.execute(request, responseHandler);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
        
        /**
         * Handles deleting of a database entry by requesting that the server
         * deletes it. Also unlistens to the data source to prevent memory
         * leaks.
         */
        public void databaseRemoved(DatabaseListChangeEvent e) {
            HttpClient httpClient = createHttpClient(projectLocation.getServiceInfo());
            try {
                SPDataSource removedDS = e.getDataSource();
                HttpDelete request = new HttpDelete(jdbcDataSourceURI(removedDS));
				httpClient.execute(request, responseHandler);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
        
        public void removeJDBCDataSourceType(JDBCDataSourceType jdst) {
            HttpClient httpClient = createHttpClient(projectLocation.getServiceInfo());
            try {
                HttpDelete request = new HttpDelete(jdbcDataSourceTypeURI(jdst));
                httpClient.execute(request, responseHandler);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
        
        private URI jdbcDataSourceURI(SPDataSource jds) throws URISyntaxException {
            if (!(jds instanceof JDBCDataSource)) throw new IllegalStateException("DataSource must be an instance of JDBCDataSource");
            
            return getServerURI(projectLocation.getServiceInfo(),
                    "/" + REST_TAG + "/data-sources/JDBCDataSource/" + jds.getName());
        }
        
        private URI olapDataSourceURI(SPDataSource jds) throws URISyntaxException {
            if (!(jds instanceof Olap4jDataSource)) throw new IllegalStateException("DataSource must be an instance of JDBCDataSource");
            
            return getServerURI(projectLocation.getServiceInfo(),
                    "/" + REST_TAG + "/data-sources/Olap4jDataSource/" + jds.getName());
        }
        
        private URI jdbcDataSourceTypeURI(JDBCDataSourceType jdst) throws URISyntaxException {
            return getServerURI(projectLocation.getServiceInfo(),
                    "/" + REST_TAG + "/data-sources/type/" + jdst.getName());
        }

        public void undoableEditHappened(UndoableEditEvent e) {
            if (e.getEdit() instanceof PlDotIni.AddDSTypeUndoableEdit) {
                JDBCDataSourceType jdst = ((PlDotIni.AddDSTypeUndoableEdit) e.getEdit()).getType();
                jdst.addPropertyChangeListener(this);
                
                List<NameValuePair> properties = new ArrayList<NameValuePair>();
                for (String name : jdst.getPropertyNames()) {
                    properties.add(new BasicNameValuePair(name, jdst.getProperty(name)));
                }
                
                postJDBCDataSourceTypeProperties(jdst, properties);
            }
            
            if (e.getEdit() instanceof PlDotIni.RemoveDSTypeUndoableEdit) {
                JDBCDataSourceType jdst = ((PlDotIni.RemoveDSTypeUndoableEdit) e.getEdit()).getType();
                jdst.removePropertyChangeListener(this);
                
                removeJDBCDataSourceType(jdst);
            }
        }
    }
	
	// ----------- Preferences accessors and mutators -----------
	// XXX Add different types, such as int or boolean, as needed

	/**
	 * Enters a double value as a preference for this server session.
	 * It will be able to be loaded by this local user in the future.
	 */
    public void putPref(String prefName, double pref) {
        prefs.putDouble(projectLocation.getUUID() + "." + prefName, pref);
    }    
    
    /**
     * Enters a String as a preference for this server session.
     * It will be able to be loaded by this local user in the future.
     */
    public void putPref(String prefName, String pref) {
        prefs.put(projectLocation.getUUID() + "." + prefName, pref);
    }
    
    /**
     * Retrieves a locally saved preference of type double.
     * @param prefName The name of the previously saved preference
     * @return The previously saved preference, or 0 if none exists yet
     */
    public double getPrefDouble(String prefName) {
        return getPrefDouble(prefName, 0);
    }
    
    /**
     * Retrieves a locally saved preference of type double.
     * @param prefName The name of the previously saved preference
     * @param def The value this function should return 
     * if no preference was previously saved
     */
    public double getPrefDouble(String prefName, double def) {
        return prefs.getDouble(projectLocation.getUUID() + "." + prefName, def);
    }
    
    /**
     * Retrieves a locally saved preference of type String.
     * @param prefName The name of the previously saved preference
     * @return The previously saved preference, or null if none exists yet
     */
    public String getPref(String prefName) {
        return getPref(prefName, null);
    }
    
    /**
     * Retrieves a locally saved preference of type String.
     * @param prefName The name of the previously saved preference
     * @param def The value this function should return 
     * if no preference was previously saved
     */
    public String getPref(String prefName, String def) {
        return prefs.get(projectLocation.getUUID() + "." + prefName, def);
    }

    public int getCurrentRevisionNumber() {
        return updater.getRevision();
    }
    
    /*
     * Do not remove this method without first changing the custom PlDotini in getDataSources().
     * Doing so will cause infinite recursion when accessing the SQLTypes.
     */
    @Override
    public List<UserDefinedSQLType> getSQLTypes() {
        // The following was my attempt to merge the snapshot and system types lists together
        // without making it O(mn), but the code is a bit lengthier than I'd like, so perhaps
        // the added complexity may not be worth it?
        List<UserDefinedSQLTypeSnapshot> typeSnapshots = 
            new ArrayList<UserDefinedSQLTypeSnapshot>(
                    getWorkspace().getChildren(UserDefinedSQLTypeSnapshot.class));
        List<UserDefinedSQLType> systemTypes = 
            new ArrayList<UserDefinedSQLType>(
                    getSystemWorkspace().getChildren(UserDefinedSQLType.class));
        
        // Remove domain snapshots from the list
        for (int i = typeSnapshots.size() - 1; i >= 0; i--) {
            UserDefinedSQLTypeSnapshot snapshot = typeSnapshots.get(i);
            if (snapshot.isDomainSnapshot()) {
                typeSnapshots.remove(i);
            }
        }
        
        // If there are no snapshots, just return the system types.
        if (typeSnapshots.size() == 0) return Collections.unmodifiableList(systemTypes);

        // Sort both lists by the UUIDs of the system types
        Collections.sort(typeSnapshots, new Comparator<UserDefinedSQLTypeSnapshot>() {
            public int compare(UserDefinedSQLTypeSnapshot o1, UserDefinedSQLTypeSnapshot o2) {
                return o1.getOriginalUUID().compareTo(o2.getOriginalUUID());
            }
        });
        Collections.sort(systemTypes, new SPObjectUUIDComparator<UserDefinedSQLType>());

        // Now go through the list of system types. If a snapshot type's
        // original UUID matches, then replace the system type with the snapshot.
        Iterator<UserDefinedSQLTypeSnapshot> snapshotIterator = typeSnapshots.iterator();
        UserDefinedSQLTypeSnapshot currentSnapshot = snapshotIterator.next();
        
        for (int i = 0; i < systemTypes.size() ; i++) {
            UserDefinedSQLType type = systemTypes.get(i);
            int compareTo = currentSnapshot.getOriginalUUID().compareTo(type.getUUID());
            if (compareTo <= 0) {
                if (compareTo == 0) {
                    systemTypes.set(i, currentSnapshot.getSPObject());
                } else {
                    systemTypes.add(i, currentSnapshot.getSPObject());
                }
                if (snapshotIterator.hasNext() && i != systemTypes.size() - 1) {
                    currentSnapshot = snapshotIterator.next();
                } else {
                    break;
                }
            }
        }
        
        // If we've gone through all the system types, then append the remaining snapshot types
        while (snapshotIterator.hasNext()) {
            currentSnapshot = snapshotIterator.next();
            systemTypes.add(currentSnapshot.getSPObject());
        }
        
        return Collections.unmodifiableList(systemTypes);
    }

    /**
     * Returns the {@link List} of {@link DomainCategory}s in this session's
     * system workspace.
     */
    @Override
    public List<DomainCategory> getDomainCategories() {
        // The following was my attempt to merge the snapshot and system category lists together
        // without making it O(nm), but the code is a bit lengthier than I'd like, so perhaps
        // the added complexity may not be worth it?
        List<DomainCategorySnapshot> categorySnapshots = 
            new ArrayList<DomainCategorySnapshot>(getWorkspace().getChildren(DomainCategorySnapshot.class));
        List<DomainCategory> systemCategories = 
            new ArrayList<DomainCategory>(getSystemWorkspace().getChildren(DomainCategory.class));
        
        // If there are no snapshots, just return the system categories.
        if (categorySnapshots.size() == 0) return Collections.unmodifiableList(systemCategories);
        
        // Sort both lists by the UUIDs of the system categories
        Collections.sort(categorySnapshots, new Comparator<DomainCategorySnapshot>() {
            public int compare(DomainCategorySnapshot o1, DomainCategorySnapshot o2) {
                return o1.getOriginalUUID().compareTo(o2.getOriginalUUID());
            }
        });
        Collections.sort(systemCategories, new SPObjectUUIDComparator<DomainCategory>());

        // Now go through the list of system categories. If a snapshot category's
        // original UUID matches, then replace the system category with the snapshot.
        Iterator<DomainCategorySnapshot> snapshotIterator = categorySnapshots.iterator();
        DomainCategorySnapshot currentSnapshot = snapshotIterator.next();
        
        for (int i = 0; i < systemCategories.size() ; i++) {
            DomainCategory type = systemCategories.get(i);
            int compareTo = currentSnapshot.getOriginalUUID().compareTo(type.getUUID());
            if (compareTo <= 0) {
                if (compareTo == 0) {
                    systemCategories.set(i, currentSnapshot.getSPObject());
                } else {
                    systemCategories.add(i, currentSnapshot.getSPObject());
                }
                if (snapshotIterator.hasNext() && i != systemCategories.size() - 1) {
                    currentSnapshot = snapshotIterator.next();
                } else {
                    break;
                }
            }
        }
        
        // If we've gone through all the system types, then append the remaining snapshot categories
        while (snapshotIterator.hasNext()) {
            currentSnapshot = snapshotIterator.next();
            systemCategories.add(currentSnapshot.getSPObject());
        }
        
        return Collections.unmodifiableList(systemCategories);
    }

    @Override
    public ArchitectSwingProject getWorkspace() {
        return (ArchitectSwingProject) super.getWorkspace();
    }
    
    /**
     * Finds the UDT that has the same uuid as the given snapshot's
     * {@link SPObjectSnapshot#getOriginalUUID()} method. The snapshot given
     * cannot be null. Returns null if no type is found.
     */
    public UserDefinedSQLType findSystemTypeFromSnapshot(@Nonnull SPObjectSnapshot<?> snapshot) {
        for (UserDefinedSQLType systemType : getSystemWorkspace().getSqlTypes()) {
            if (systemType.getUUID().equals(snapshot.getOriginalUUID())) {
                return systemType;
            }
        }
        for (DomainCategory category : getSystemWorkspace().getDomainCategories()) {
            for (UserDefinedSQLType systemType : category.getChildren(UserDefinedSQLType.class)) {
                if (systemType.getUUID().equals(snapshot.getOriginalUUID())) {
                    return systemType;
                }
            }
        }
        return null;
    }

    /**
     * Returns an action that will update the snapshot object in the given
     * snapshot to be the same as the type the snapshot maps to in the system
     * workspace.
     * 
     * @param snapshot
     *            The snapshot to update. This currently only works for
     *            snapshots of UserDefinedSQLTypes but will be extended to
     *            others in the future.
     */
    public Runnable createUpdateSnapshotRunnable(final SPObjectSnapshot<?> snapshot) {
        return new Runnable() {

            @Override
            public void run() {
                if (!(snapshot.getSPObject() instanceof UserDefinedSQLType)) return;
                UserDefinedSQLType snapshotType = (UserDefinedSQLType) snapshot.getSPObject();
                UserDefinedSQLType systemType = findSystemTypeFromSnapshot(snapshot);
                if (systemType == null) return;
                
                snapshotType.updateToMatch(systemType);
                snapshot.setObsolete(false);
            }
        };
    }
}
