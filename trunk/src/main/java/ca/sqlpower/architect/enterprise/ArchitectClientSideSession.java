package ca.sqlpower.architect.enterprise;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

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

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersisterListener;
import ca.sqlpower.dao.SPSessionPersister;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.dao.json.SPJSONPersister;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.diff.DiffInfo;
import ca.sqlpower.diff.SimpleDiffChunkJSONConverter;
import ca.sqlpower.enterprise.TransactionInformation;
import ca.sqlpower.enterprise.client.RevisionController;
import ca.sqlpower.enterprise.client.SPServerInfo;
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
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.SQLPowerUtils;
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
	private final SPSessionPersister sessionPersister;
	
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
		super(context, name);
		
		this.projectLocation = projectLocation;
		this.isEnterpriseSession = true;
		
		String ddlgClass = prefs.get(this.projectLocation.getUUID() + ".ddlg", null);
		if (ddlgClass != null) {
		    try {
                DDLGenerator ddlg = (DDLGenerator) Class.forName(ddlgClass).newInstance();
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
		        new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		sessionPersister.setWorkspaceContainer(this);
		
		jsonMessageDecoder = new SPJSONMessageDecoder(sessionPersister);
		
		updater = new NetworkConflictResolver(
		        projectLocation, 
		        jsonMessageDecoder, 
		        createHttpClient(projectLocation.getServiceInfo()), 
		        outboundHttpClient, this);
		
		jsonPersister = new SPJSONPersister(updater);
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
					plIni = new PlDotIni(
                    		getServerURI(projectLocation.getServiceInfo(), "/jdbc/"),
                    		getServerURI(projectLocation.getServiceInfo(), MONDRIAN_SCHEMA_REL_PATH));
                    plIni.read(response.getEntity().getContent());
                    logger.debug("Data source collection has URI " + plIni.getServerBaseURI());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                
                return new SpecificDataSourceCollection<JDBCDataSource>(plIni, JDBCDataSource.class);
            }
        };
        
        try {
            dataSourceCollection = executeServerRequest(outboundHttpClient, projectLocation.getServiceInfo(), "/data-sources/", plIniHandler);
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
						new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		SQLPowerUtils.listenToHierarchy(getWorkspace(), listener);
		
		updater.setListener(listener);
		updater.setConverter(new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		updater.start();
		
		addSessionLifecycleListener(new SessionLifecycleListener<ArchitectSession>() {
			public void sessionClosing(SessionLifecycleEvent<ArchitectSession> e) {
				SQLPowerUtils.unlistenToHierarchy(getWorkspace(), listener);
			}
		});
	}
	
	public void persistProjectToServer() throws SPPersistenceException {
		final SPPersisterListener tempListener = new SPPersisterListener(jsonPersister,
						new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		tempListener.persistObject(getWorkspace(), 0);
	}
	
	public ArchitectProject getSystemWorkspace() {
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
	
	public static List<ProjectLocation> getWorkspaceNames(SPServerInfo serviceInfo, ArchitectSession session) 
	throws IOException, URISyntaxException, JSONException {
    	HttpClient httpClient = createHttpClient(serviceInfo);
    	try {
    		HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, "/jcr/projects"));
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
                    "/project/" + projectLocation.getUUID() + "/revision_list",
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
    		HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, "/jcr/projects/new", "name=" + name));
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
                    "/project/" + projectLocation.getUUID() + "/revert",
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
                    "/project/" + projectLocation.getUUID() + "/" + revisionNo,
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
                    "/project/" + projectLocation.getUUID() + "/compare",
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
                    TransactionInformation.DATE_FORMAT.parse(jsonItem.getString("time")),
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
    				"/jcr/" + projectLocation.getUUID() + "/delete", 
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
                    "/data-sources/JDBCDataSource/" + jds.getName());
        }
        
        private URI olapDataSourceURI(SPDataSource jds) throws URISyntaxException {
            if (!(jds instanceof Olap4jDataSource)) throw new IllegalStateException("DataSource must be an instance of JDBCDataSource");
            
            return getServerURI(projectLocation.getServiceInfo(),
                    "/data-sources/Olap4jDataSource/" + jds.getName());
        }
        
        private URI jdbcDataSourceTypeURI(JDBCDataSourceType jdst) throws URISyntaxException {
            return getServerURI(projectLocation.getServiceInfo(),
                    "/data-sources/type/" + jdst.getName());
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
}
