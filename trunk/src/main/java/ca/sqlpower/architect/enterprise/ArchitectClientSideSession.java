package ca.sqlpower.architect.enterprise;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olap4j.metadata.Datatype;
import org.springframework.security.AccessDeniedException;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.SnapshotCollection;
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
import ca.sqlpower.enterprise.ClientSideSessionUtils;
import ca.sqlpower.enterprise.DataSourceCollectionUpdater;
import ca.sqlpower.enterprise.JSONMessage;
import ca.sqlpower.enterprise.JSONResponseHandler;
import ca.sqlpower.enterprise.ServerInfoProvider;
import ca.sqlpower.enterprise.TransactionInformation;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.enterprise.client.RevisionController;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.object.SPObjectUUIDComparator;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.sqlobject.UserDefinedSQLTypeSnapshot;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;

public class ArchitectClientSideSession extends ArchitectSessionImpl implements RevisionController {	
	
	private static Logger logger = Logger.getLogger(ArchitectClientSideSession.class);
	
	private static CookieStore cookieStore = new BasicCookieStore();
	
	public static final String MONDRIAN_SCHEMA_REL_PATH = "/mondrian";
	
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
	private final ArchitectNetworkConflictResolver updater;
	private final SPJSONMessageDecoder jsonMessageDecoder;
	private final DataSourceCollectionUpdater dataSourceCollectionUpdater;
	
	private DataSourceCollection <JDBCDataSource> dataSourceCollection;

	/**
	 * Used to store sessions which hold nothing but security info.
	 */
	public static Map<String, ArchitectClientSideSession> securitySessions;
	
    private AbstractPoolingSPListener deletionListener;

    /**
     * The executor to use as the foreground thread manager. If this is not null
     * and there is no EDT available this executor will be used to ensure the
     * system is single threaded.
     */
    private final ThreadPoolExecutor foregroundThreadExecutor;

    /**
     * This is false except for testing purposes when the single thread executor
     * may want to be used if the test is running headless.
     */
    private final boolean useThreadPool;
    
    /**
     * The threads used by {@link #foregroundThreadExecutor} to keep Architect
     * single threaded if we are using the {@link #foregroundThreadExecutor}.
     * The executor will only allow one of these threads to be used at a time.
     */
    private final Set<Thread> foregroundExecutorThread = Collections.synchronizedSet(new HashSet<Thread>());
    
    static {
        securitySessions = new HashMap<String, ArchitectClientSideSession>();
    }
	
	public ArchitectClientSideSession(ArchitectSessionContext context, 
			String name, ProjectLocation projectLocation) throws SQLObjectException {
	    this(context, name, projectLocation, false);
	}

    /**
     * This constructor is only used for testing. This constructor allows users
     * to specify an executor to use as the foreground thread instead of using
     * the normal EDT. This is handy for ensuring all of the events occur on the
     * correct thread and updates do not conflict with persists. If the executor
     * is null then the foreground thread will just execute the runnables on the
     * current thread.
     */
	public ArchitectClientSideSession(ArchitectSessionContext context,
	        String name, ProjectLocation projectLocation, boolean useThreadPool) throws SQLObjectException {
		super(context, name, new ArchitectSwingProject());
		
		this.projectLocation = projectLocation;
        this.useThreadPool = useThreadPool;
        this.foregroundThreadExecutor = new ThreadPoolExecutor(1, 1, 5, TimeUnit.MINUTES, 
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread newThread = new Thread(r);
                        foregroundExecutorThread.add(newThread);
                        return newThread;
                    }
                });
        foregroundThreadExecutor.allowCoreThreadTimeOut(false);
		dataSourceCollectionUpdater = new ArchitectDataSourceCollectionUpdater(projectLocation);
		this.isEnterpriseSession = true;
		
		setupSnapshots();
		
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
		
		outboundHttpClient = ClientSideSessionUtils.createHttpClient(projectLocation.getServiceInfo(), cookieStore);
		dataSourceCollection = getDataSources();
		
		sessionPersister = new ArchitectSessionPersister("inbound-" + projectLocation.getUUID(), getWorkspace(), 
		        new ArchitectPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		sessionPersister.setWorkspaceContainer(this);
		
		jsonMessageDecoder = new SPJSONMessageDecoder(sessionPersister);
		
		updater = new ArchitectNetworkConflictResolver(
		        projectLocation, 
		        jsonMessageDecoder, 
		        ClientSideSessionUtils.createHttpClient(projectLocation.getServiceInfo(), cookieStore), 
		        outboundHttpClient, this);
		
		jsonPersister = new SPJSONPersister(updater);
		
		verifyServerLicense(projectLocation);
	}

    protected void verifyServerLicense(ProjectLocation projectLocation) throws AssertionError {
        try {
            ServerInfoProvider.getServerVersion(
                    projectLocation.getServiceInfo().getServerAddress(), 
                    String.valueOf(projectLocation.getServiceInfo().getPort()), 
                    projectLocation.getServiceInfo().getPath(), 
                    projectLocation.getServiceInfo().getUsername(), 
                    projectLocation.getServiceInfo().getPassword(),
                    cookieStore);
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
        final AbstractPoolingSPListener obsolescenceListener = new AbstractPoolingSPListener(false) {
            @Override
            public void childAddedImpl(SPChildEvent e) {
                if (e.getChild() instanceof UserDefinedSQLTypeSnapshot) {
                    UserDefinedSQLTypeSnapshot snapshot = (UserDefinedSQLTypeSnapshot) e.getChild();
                    UserDefinedSQLType systemType = findSystemTypeFromSnapshot(snapshot);
                    if (systemType == null) {
                        snapshot.setDeleted(true);
                    } else { 
                        if (!UserDefinedSQLType.areEqual(snapshot.getSPObject(), systemType)) {
                            snapshot.setObsolete(true);
                        }
                        snapshot.setDeleted(false);
                    }
                } else if (e.getChild() instanceof DomainCategorySnapshot) {
                    DomainCategorySnapshot snapshot = (DomainCategorySnapshot) e.getChild();
                    boolean deleted = true;
                    for (DomainCategory category : getSystemWorkspace().getDomainCategories()) {
                        if (category.getUUID().equals(snapshot.getOriginalUUID())) {
                            deleted = false;
                            if (!DomainCategory.areEqual(snapshot.getSPObject(), category)) {
                                snapshot.setObsolete(true);
                                deleted = true;
                            }
                        }
                    }
                    snapshot.setDeleted(deleted);
                }
            }
        };
        getWorkspace().getSnapshotCollection().addSPListener(obsolescenceListener);
        getWorkspace().addSPListener(new AbstractSPListener() {
            @Override
            public void transactionStarted(TransactionEvent e) {
                obsolescenceListener.transactionStarted(TransactionEvent.createStartTransactionEvent(getWorkspace().getSnapshotCollection(), "Simulated begin: " + e.getMessage()));
            }
            @Override
            public void transactionEnded(TransactionEvent e) {
                obsolescenceListener.transactionEnded(TransactionEvent.createEndTransactionEvent(getWorkspace().getSnapshotCollection(), "Simulated commit: " + e.getMessage()));
            }
            @Override
            public void transactionRollback(TransactionEvent e) {
                obsolescenceListener.transactionRollback(TransactionEvent.createRollbackTransactionEvent(getWorkspace().getSnapshotCollection(), "Simulated rollback: " + e.getMessage()));
            }
        });
        
        // If this returns null, it means the system session hasn't been
        // initialized yet. This means we are the system session, and attaching
        // the listener is unnecessary.
        if (getSystemSession() != null) {
            deletionListener = new AbstractPoolingSPListener() {
                @Override
                protected void childAddedImpl(SPChildEvent e) {
                    if (e.getChild() instanceof DomainCategory) {
                        e.getChild().addSPListener(deletionListener);
                    } else {
                        for (SPObjectSnapshot<SPObject> snapshot : getWorkspace().getSnapshotCollection().getChildren(SPObjectSnapshot.class)) {
                            if (snapshot.getOriginalUUID().equals(e.getChild().getUUID())) {
                                snapshot.setDeleted(false);
                            }
                        }
                    }
                }

                @Override
                protected void childRemovedImpl(SPChildEvent e) {
                    if (e.getChild() instanceof DomainCategory) {
                        e.getChild().removeSPListener(deletionListener);
                    } else {
                        for (SPObjectSnapshot<SPObject> snapshot : getWorkspace().getSnapshotCollection().getChildren(SPObjectSnapshot.class)) {
                            if (snapshot.getOriginalUUID().equals(e.getChild().getUUID())) {
                                snapshot.setDeleted(true);
                            }
                        }
                    }
                }
            };

            getSystemWorkspace().addSPListener(deletionListener);
            for (DomainCategory cat : getSystemWorkspace().getChildren(DomainCategory.class)) {
                cat.addSPListener(deletionListener);
            }
        }
    }

    /**
     * Map of server addresses to system workspaces. Use
     * {@link SPServerInfo#getServerAddress()} as the key.
     */
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
        
        getSystemWorkspace().removeSPListener(deletionListener);
        for (DomainCategory cat : getSystemWorkspace().getChildren(DomainCategory.class)) {
            cat.removeSPListener(deletionListener);
        }
        
        return super.close();
    }

	@Override
	public DataSourceCollection<JDBCDataSource> getDataSources() {
	    if (dataSourceCollection == null) {
            dataSourceCollection = getDataSourcesFromServer();
            dataSourceCollectionUpdater.attach(dataSourceCollection);
        }
	    return dataSourceCollection;
	}
	
	private DataSourceCollection<JDBCDataSource> getDataSourcesFromServer() {
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
					plIni = new PlDotIni(ClientSideSessionUtils.getServerURI(projectLocation.getServiceInfo(), "/" + ClientSideSessionUtils.REST_TAG +"/jdbc/"),
					        ClientSideSessionUtils.getServerURI(projectLocation.getServiceInfo(), MONDRIAN_SCHEMA_REL_PATH)) {
					    
					    @Override
					    public List<UserDefinedSQLType> getSQLTypes() {
					        List<UserDefinedSQLType> types = new ArrayList<UserDefinedSQLType>();
					        types.addAll(ArchitectClientSideSession.this.getSQLTypes());
					        types.addAll(ArchitectClientSideSession.this.getDomains());
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
					    
					    public SPDataSource getDataSource(String name) {
					        SPDataSource ds = super.getDataSource(name);
					        if (ds == null) {
					            mergeNewDataSources();
					            return super.getDataSource(name);
					        } else {
					            return ds;
					        }
					    }
					    
					    public <C extends SPDataSource> C getDataSource(String name, java.lang.Class<C> classType) {
					        C ds = super.getDataSource(name, classType);
                            if (ds == null) {
                                mergeNewDataSources();
                                return super.getDataSource(name, classType);
                            } else {
                                return ds;
                            }
					    }
					    
					    private void mergeNewDataSources() {
					        DataSourceCollection<JDBCDataSource> dsc = getDataSourcesFromServer();
                            for (SPDataSource merge : dsc.getConnections()) {
                                mergeDataSource(merge);
                            }
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
        
        DataSourceCollection<JDBCDataSource> dsc;
        try {
            dsc = ClientSideSessionUtils.executeServerRequest(outboundHttpClient, projectLocation.getServiceInfo(), 
                    "/" + ClientSideSessionUtils.REST_TAG + "/data-sources/", plIniHandler);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        return dsc;
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
		} else if (useThreadPool) {
		    foregroundThreadExecutor.execute(runner);
		} else {
			super.runInForeground(runner);
		}
	}
	
	@Override
	public boolean isForegroundThread() {
	    if (useThreadPool) {
	        return foregroundExecutorThread.contains(Thread.currentThread());
	    } else {
	        return super.isForegroundThread();
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
	
	public List<TransactionInformation> getTransactionList(long fromVersion, long toVersion)
    throws IOException, URISyntaxException, JSONException, ParseException {
        
        SPServerInfo serviceInfo = projectLocation.getServiceInfo();
        HttpClient httpClient = ClientSideSessionUtils.createHttpClient(serviceInfo, cookieStore);
        
        try {
            
            logger.info("Getting transactions between " + fromVersion + " and " + toVersion);
            JSONMessage message = ClientSideSessionUtils.executeServerRequest(httpClient, projectLocation.getServiceInfo(),
                    "/" + ClientSideSessionUtils.REST_TAG + "/project/" + projectLocation.getUUID() + "/revision_list",
                    "versions=" + fromVersion + ":" + toVersion,
                    new JSONResponseHandler());
            
            return ClientSideSessionUtils.decodeJSONRevisionList(message.getBody());
            
        } finally {
            httpClient.getConnectionManager().shutdown();
        }               
        
    }

	public static ProjectLocation uploadProject(SPServerInfo serviceInfo, String name, File project, UserPrompterFactory session) 
    throws URISyntaxException, ClientProtocolException, IOException, JSONException {
	    return ClientSideSessionUtils.uploadProject(serviceInfo, name, project, session, cookieStore);
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
        return ClientSideSessionUtils.revertServerWorkspace(projectLocation, revisionNo, cookieStore);
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
	public void updateUserPassword(User user, 
	        String oldPassword, String newPassword, UserPrompterFactory upf) {
	    SPServerInfo serviceInfo = getProjectLocation().getServiceInfo();
        
        HttpClient client = ClientSideSessionUtils.createHttpClient(serviceInfo, cookieStore);
        
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        
        try {
            JSONObject begin = new JSONObject();
            begin.put("uuid", JSONObject.NULL);
            begin.put("method", "begin");
            
            JSONObject persist = new JSONObject();
            persist.put("uuid", user.getUUID());
            persist.put("propertyName", "password");
            persist.put("type", Datatype.STRING.toString());
            if (oldPassword == null) {
                persist.put("method", "persistProperty");
            } else {
                persist.put("method", "changeProperty");
                persist.put("oldValue", new String(Hex.encodeHex(digester.digest(oldPassword.getBytes()))));
            }
            persist.put("newValue", new String(Hex.encodeHex(digester.digest(newPassword.getBytes()))));
            
            JSONObject commit = new JSONObject();
            commit.put("uuid", JSONObject.NULL);
            commit.put("method", "commit");
            
            JSONArray transaction = new JSONArray();
            transaction.put(begin);
            transaction.put(persist);
            transaction.put(commit);

            URI serverURI = new URI("http", null, 
                    serviceInfo.getServerAddress(), 
                    serviceInfo.getPort(),
                    serviceInfo.getPath() + 
                    "/" + ClientSideSessionUtils.REST_TAG + "/project/system", 
                    "currentRevision=" + getCurrentRevisionNumber(), null);
            HttpPost postRequest = new HttpPost(serverURI);
            postRequest.setEntity(new StringEntity(transaction.toString())); 
            postRequest.setHeader("Content-Type", "application/json");
            HttpUriRequest request = postRequest;
            JSONMessage result = client.execute(request, new JSONResponseHandler());
            if (result.getStatusCode() != 200) {
                logger.warn("Failed password change");
                if (result.getStatusCode() == 412) {
                    upf.createUserPrompter("The password you have entered is incorrect.", 
                            UserPromptType.MESSAGE, 
                            UserPromptOptions.OK, 
                            UserPromptResponse.OK, 
                            "OK", "OK").promptUser("");
                } else {
                    upf.createUserPrompter(
                            "Could not change the password due to the following: " + 
                            result.getBody() + " See logs for more details.", 
                            UserPromptType.MESSAGE, 
                            UserPromptOptions.OK, 
                            UserPromptResponse.OK, 
                            "OK", "OK").promptUser("");
                }
            } else {
                upf.createUserPrompter(
                        "Password successfully changed. Please log into open projects" +
                        " with your new password.", 
                        UserPromptType.MESSAGE, 
                        UserPromptOptions.OK, 
                        UserPromptResponse.OK, 
                        "OK", "OK").promptUser("");
            }
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
	    persistRevisionFromServer(projectLocation, revisionNo, targetDecoder);
	}

	/**
     * Gets a list of DiffChunks representing the differences between the two revisions from the server.
     */
	public List<DiffChunk<DiffInfo>> getComparisonDiffChunks(int oldRevisionNo, int newRevisionNo) 
	throws IOException, URISyntaxException, JSONException, SPPersistenceException {
	    
        SPServerInfo serviceInfo = projectLocation.getServiceInfo();
        HttpClient httpClient = ClientSideSessionUtils.createHttpClient(serviceInfo, cookieStore);
        
        try {
            JSONMessage response = ClientSideSessionUtils.executeServerRequest(httpClient, projectLocation.getServiceInfo(),
                    "/" + ClientSideSessionUtils.REST_TAG + "/project/" + projectLocation.getUUID() + "/compare",
                    "versions=" + oldRevisionNo + ":" + newRevisionNo, 
                    new JSONResponseHandler());    
                                  
            return SimpleDiffChunkJSONConverter.decode(response.getBody());
            
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        
	}
    
    public static HttpClient createHttpClient(SPServerInfo serviceInfo) {
        return ClientSideSessionUtils.createHttpClient(serviceInfo, cookieStore);
    }
    
    public static void persistRevisionFromServer(ProjectLocation projectLocation, 
            int revisionNo, 
            SPJSONMessageDecoder decoder)
    throws IOException, URISyntaxException, SPPersistenceException, IllegalArgumentException {
        
        ClientSideSessionUtils.persistRevisionFromServer(projectLocation, revisionNo, decoder, cookieStore);
    }
	
	public static ProjectLocation createNewServerSession(SPServerInfo serviceInfo, String name, 
	        ArchitectSession session)
    throws URISyntaxException, ClientProtocolException, IOException, JSONException {
        return ClientSideSessionUtils.createNewServerSession(serviceInfo,
                name,
                cookieStore,
                new DefaultUserPrompterFactory());
    }
    
    public static List<ProjectLocation> getWorkspaceNames(SPServerInfo serviceInfo,
            UserPrompterFactory upf) 
    throws IOException, URISyntaxException, JSONException {
        return ClientSideSessionUtils.getWorkspaceNames(serviceInfo, cookieStore, upf);
    }
	
	public static void deleteServerWorkspace(ProjectLocation projectLocation, ArchitectSession session) throws URISyntaxException, ClientProtocolException, IOException {
    	
	    ClientSideSessionUtils.deleteServerWorkspace(projectLocation,
	            cookieStore, new DefaultUserPrompterFactory());
    }
    
    public ArchitectNetworkConflictResolver getUpdater() {
        return updater;
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
        SnapshotCollection collection = getWorkspace().getSnapshotCollection();
        
        List<UserDefinedSQLTypeSnapshot> typeSnapshots = 
            new ArrayList<UserDefinedSQLTypeSnapshot>(
                   collection.getChildren(UserDefinedSQLTypeSnapshot.class));
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
     * Returns the {@link List} of {@link UserDefinedSQLType}s in this session
     * that are children of a {@link DomainCategory}. Each domain in the system
     * will be in the list once as either the domain that is in the system
     * workspace or a snapshot of the domain that is in the current project. UDTs
     * that are children of a category are defined as domains instead of types.
     */
    @Override
    public List<UserDefinedSQLType> getDomains() {
        // The following was my attempt to merge the snapshot and system category lists together
        // without making it O(nm), but the code is a bit lengthier than I'd like, so perhaps
        // the added complexity may not be worth it?
        SnapshotCollection collection = getWorkspace().getSnapshotCollection();
        
        List<UserDefinedSQLTypeSnapshot> typeSnapshots = 
            new ArrayList<UserDefinedSQLTypeSnapshot>(collection.getChildren(UserDefinedSQLTypeSnapshot.class));
        List<DomainCategory> systemCategories = 
            new ArrayList<DomainCategory>(getSystemWorkspace().getChildren(DomainCategory.class));
        List<UserDefinedSQLTypeSnapshot> domainSnapshots = new ArrayList<UserDefinedSQLTypeSnapshot>();
        for (UserDefinedSQLTypeSnapshot udtSnapshot : typeSnapshots) {
            if (udtSnapshot.isDomainSnapshot()) {
                domainSnapshots.add(udtSnapshot);
            }
        }
        List<UserDefinedSQLType> systemDomains = new ArrayList<UserDefinedSQLType>();
        for (DomainCategory category : systemCategories) {
            systemDomains.addAll(category.getChildren(UserDefinedSQLType.class));
        }
        
        // If there are no snapshots, just return the system categories.
        if (domainSnapshots.size() == 0) return Collections.unmodifiableList(systemDomains);
        
        // Sort both lists by the UUIDs of the system categories
        Collections.sort(domainSnapshots, new Comparator<UserDefinedSQLTypeSnapshot>() {
            public int compare(UserDefinedSQLTypeSnapshot o1, UserDefinedSQLTypeSnapshot o2) {
                return o1.getOriginalUUID().compareTo(o2.getOriginalUUID());
            }
        });
        Collections.sort(systemDomains, new SPObjectUUIDComparator<UserDefinedSQLType>());

        // Now go through the list of system categories. If a snapshot category's
        // original UUID matches, then replace the system category with the snapshot.
        Iterator<UserDefinedSQLTypeSnapshot> snapshotIterator = domainSnapshots.iterator();
        UserDefinedSQLTypeSnapshot currentSnapshot = snapshotIterator.next();
        
        for (int i = 0; i < systemDomains.size() ; i++) {
            UserDefinedSQLType type = systemDomains.get(i);
            int compareTo = currentSnapshot.getOriginalUUID().compareTo(type.getUUID());
            if (compareTo <= 0) {
                if (compareTo == 0) {
                    systemDomains.set(i, currentSnapshot.getSPObject());
                } else {
                    systemDomains.add(i, currentSnapshot.getSPObject());
                }
                if (snapshotIterator.hasNext() && i != systemDomains.size() - 1) {
                    currentSnapshot = snapshotIterator.next();
                } else {
                    break;
                }
            }
        }
        
        // If we've gone through all the system types, then append the remaining snapshot categories
        while (snapshotIterator.hasNext()) {
            currentSnapshot = snapshotIterator.next();
            systemDomains.add(currentSnapshot.getSPObject());
        }
        
        return Collections.unmodifiableList(systemDomains);
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
                if (snapshot.getSPObject() instanceof DomainCategory) {
                    for (DomainCategory category : getSystemWorkspace().getDomainCategories()) {
                        if (category.getUUID().equals(snapshot.getOriginalUUID())) {
                            ((DomainCategory) snapshot.getSPObject()).updateToMatch(category);
                            snapshot.setObsolete(false);
                            return;
                        }
                    }
                }
                
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
