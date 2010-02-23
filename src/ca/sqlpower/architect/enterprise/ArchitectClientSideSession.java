package ca.sqlpower.architect.enterprise;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
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

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.dao.HttpMessageSender;
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
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sql.JDBCDataSource;
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

public class ArchitectClientSideSession extends ArchitectSessionImpl {	
	
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
	private final Updater updater;
	private final Sender sender;
	private final SPJSONMessageDecoder jsonMessageDecoder;
	private final DataSourceCollectionUpdater dataSourceCollectionUpdater = new DataSourceCollectionUpdater();
	
	private DataSourceCollection <JDBCDataSource> dataSourceCollection;
	
	// -

    /**
     * The revision this project is currently at. This will be updated as
     * changes come in from the server.
     */
	int currentRevision = 0;
	
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
		
		sender = new Sender(outboundHttpClient, projectLocation.getServiceInfo(), projectLocation.getUUID());
		jsonPersister = new SPJSONPersister(sender);
		
		dataSourceCollection = getDataSourceCollection();
		
		sessionPersister = new ArchitectSessionPersister("inbound-" + projectLocation.getUUID(), getWorkspace(), 
				new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		sessionPersister.setSession(this);
		
		jsonMessageDecoder = new SPJSONMessageDecoder(sessionPersister);
		updater = new Updater(projectLocation.getUUID(), jsonMessageDecoder);
	}

	// -
	
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
    	    getContext().getSessions().remove(this);
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
		
        outboundHttpClient.getConnectionManager().shutdown();
        updater.interrupt();
        
        if (dataSourceCollection != null) {
            dataSourceCollectionUpdater.detach(dataSourceCollection);
        }
        
        return super.close();
    }
	
	public DataSourceCollection <JDBCDataSource> getDataSourceCollection () {
		if (dataSourceCollection != null) {
			return dataSourceCollection;
		}
		
		ResponseHandler<DataSourceCollection<JDBCDataSource>> plIniHandler = 
            new ResponseHandler<DataSourceCollection<JDBCDataSource>>() {
            public DataSourceCollection<JDBCDataSource> handleResponse(HttpResponse response)
                    throws ClientProtocolException, IOException {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException(
                            "Server error while reading data sources: " + response.getStatusLine());
                }
                PlDotIni plIni;
                try {
					plIni = new PlDotIni(
                    		getServerURI(projectLocation.getServiceInfo(), "/jdbc"),
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        dataSourceCollectionUpdater.attach(dataSourceCollection);
        
        return dataSourceCollection;
	}
	
	public void startUpdaterThread() {
		updater.start();
		
		final SPPersisterListener listener = new SPPersisterListener(jsonPersister, sessionPersister,
						new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		
		SQLPowerUtils.listenToHierarchy(getWorkspace(), listener);
		
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
		for (ArchitectSession session : this.getContext().getSessions()) {
			if (session.getWorkspace().getUUID().equals("system")) {
				return session.getWorkspace();
			}
		}
		return null;
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
    	} finally {
    		httpClient.getConnectionManager().shutdown();
    	}
    }
	
	/**
     * Requests the server for the transaction list. 
     * 
     * @return A list of TransactionInformation containing all the information about revisions of this project.
     * @throws IOException
     * @throws URISyntaxException
     * @throws JSONException
     * @throws ParseException
     */
    public List<TransactionInformation> getTransactionList() throws IOException, URISyntaxException, JSONException, ParseException {
        
        SPServerInfo serviceInfo = projectLocation.getServiceInfo();
        HttpClient httpClient = createHttpClient(serviceInfo);
        List<TransactionInformation> transactions = new ArrayList<TransactionInformation>();
        
        try {
            
            JSONMessage message = executeServerRequest(httpClient, projectLocation.getServiceInfo(),
                    "/project/" + projectLocation.getUUID() + "/revision_list", 
                    new JSONResponseHandler());
            JSONArray jsonArray = new JSONArray(message.getBody());
            
            for (int i = 0; i < jsonArray.length(); i++) {
                
                JSONObject json = jsonArray.getJSONObject(i);
                TransactionInformation transaction = new TransactionInformation(
                        json.getLong("number"),                     
                        TransactionInformation.DATE_FORMAT.parse(json.getString("time")),
                        json.getString("author"),
                        json.getString("description"));
                transactions.add(transaction);
                
            }
                                 
            return transactions;
            
        } finally {
            httpClient.getConnectionManager().shutdown();
        }               
        
    }

    public static ProjectLocation createNewServerSession(SPServerInfo serviceInfo, String name) throws URISyntaxException, ClientProtocolException, IOException, JSONException {
    	HttpClient httpClient = createHttpClient(serviceInfo);
    	try {
    		HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, "/jcr/projects/new", "name=" + name));
    		JSONMessage message = httpClient.execute(request, new JSONResponseHandler());
    		JSONObject response = new JSONObject(message.getBody());
    		return new ProjectLocation(
    					response.getString("uuid"),
    					response.getString("name"),
    					serviceInfo);
    	} finally {
    		httpClient.getConnectionManager().shutdown();
    	}
    }
	
	public void revertServerWorkspace(int revisionNo) throws IOException, URISyntaxException {
	    revertServerWorkspace(projectLocation, revisionNo);
	}
	
	/**
	 * This method reverts the server workspace specified by the given project location
	 * to the specified revision number.
	 * 
	 * All sessions should automatically update to the reverted revision due to their Updater.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void revertServerWorkspace(ProjectLocation projectLocation, int revisionNo) throws IOException, URISyntaxException {
        SPServerInfo serviceInfo = projectLocation.getServiceInfo();
        HttpClient httpClient = createHttpClient(serviceInfo);
        
        try {
            executeServerRequest(httpClient, projectLocation.getServiceInfo(),
                    "/project/" + projectLocation.getUUID() + "/revert",
                    "revisionNo=" + revisionNo, 
                    new JSONResponseHandler());       
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
	
	public static void deleteServerWorkspace(ProjectLocation projectLocation) throws URISyntaxException, ClientProtocolException, IOException {
    	SPServerInfo serviceInfo = projectLocation.getServiceInfo();
    	HttpClient httpClient = createHttpClient(serviceInfo);
    	
    	try {
    		executeServerRequest(httpClient, projectLocation.getServiceInfo(),
    				"/jcr/" + projectLocation.getUUID() + "/delete", 
    				new BasicResponseHandler());
    		
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
	
	// Contained classes --------------------------------------------------------------------
	
	boolean persistingToServer = false;
	
	/**
	 * Sends outgoing JSON
	 */
	private class Sender extends HttpMessageSender<JSONObject> {

		private JSONArray message;
		
		public Sender(HttpClient httpClient, SPServerInfo serverInfo, String rootUUID) {
			super(httpClient, serverInfo, rootUUID);
			message = new JSONArray();
		}

		public void clear() {
			message = new JSONArray();
		}

		private boolean flushAgain = false;
		public void flush() throws SPPersistenceException {
		    try {
		        if (persistingToServer) {
		            flushAgain = true;
		            return;
		        }

		        persistingToServer = true;
		        flushAgain = false;
    		    
    		    URI serverURI = getServerURI();
                HttpPost postRequest = new HttpPost(serverURI);
                
                postRequest.setEntity(new StringEntity(message.toString()));
                postRequest.setHeader("Content-Type", "application/json");
                HttpUriRequest request = postRequest;
                clear();
                
                final JSONMessage response = getHttpClient().execute(request, new JSONResponseHandler());
    		    runInForeground(new Runnable() {
    		        public void run() {
            		    try {
            		        if (response.isSuccessful()) {
            		            // Message was sent successfully and accepted by the server.
            		            currentRevision = (new JSONObject(response.getBody())).getInt("currentRevision");
            		        } else {
            		            // Message was sent successfully but rejected by the server. We must rollback our
            		            // changes and update to the head revision.
            		            logger.debug("Response unsuccessful");
            		            throw new RuntimeException("Out of sync with server");
            		        }
            			} catch (JSONException e) {
                            throw new RuntimeException(e);
                        } finally {
            				persistingToServer = false;
                            try {
                                if (flushAgain) flush();
                            } catch (SPPersistenceException e) {
                                throw new RuntimeException(e);
                            }
            			}
                    }
                });    
		    } catch (UnsupportedEncodingException e) {
		        throw new SPPersistenceException(null, e);
            } catch (URISyntaxException e) {
                throw new SPPersistenceException(null, e);
            } catch (ClientProtocolException e) {
                throw new SPPersistenceException(null, e);
            } catch (IOException e) {
                throw new SPPersistenceException(null, e);
            } catch (RuntimeException e) {
                throw new SPPersistenceException(null, e);
            }
		}

		public void send(JSONObject content) throws SPPersistenceException {
			message.put(content);
		}
		
		public URI getServerURI() throws URISyntaxException {
			String contextPath = getServerInfo().getPath();
	        return new URI("http", null, getServerInfo().getServerAddress(), getServerInfo().getPort(),
	                contextPath + "/project/" + getProjectLocation().getUUID(), "currentRevision=" + currentRevision, null);
		}
	}
	
	
	/**
	 * Polls this session's server for updates until interrupted. There should
	 * be exactly one instance of this class per ArchitectServerSession.
	 */
	private class Updater extends Thread {
		
		/**
		 * How long we will pause after an update error before attempting to
		 * contact the server again.
		 */
		private long retryDelay = 1000;
		
		private final SPJSONMessageDecoder jsonDecoder;

		/**
		 * Used by the Updater to handle inbound HTTP updates
		 */
		private final HttpClient inboundHttpClient;
		
		final String contextRelativePath = "/project/" + projectLocation.getUUID();

		private volatile boolean cancelled;
		
		/**
		 * Creates, but does not start, the updater thread.
		 * 
		 * @param projectUUID
		 *            the ID of the workspace this updater is responsible for. This is
		 *            used in creating the thread's name.
		 */
		Updater(String projectUUID, SPJSONMessageDecoder jsonDecoder) {
			super("updater-" + projectUUID);
			this.jsonDecoder = jsonDecoder;
			inboundHttpClient = createHttpClient(projectLocation.getServiceInfo());
		}
		
		public void interrupt() {
			super.interrupt();
			cancelled = true;
		}
        
		boolean updating = false;
		
		@Override
		public void run() {
			logger.info("Updater thread starting");
			try {
				while (!this.isInterrupted() && !cancelled) {
					try {
					    while (updating) {
					        synchronized (this) {
					            wait();
					        }
					    }
					    
					    URI uri = getServerURI(projectLocation.getServiceInfo(), contextRelativePath, 
					            "oldRevisionNo=" + currentRevision);
					    HttpUriRequest request = new HttpGet(uri);
	                        
                        JSONMessage message = inboundHttpClient.execute(request, new JSONResponseHandler());                     
                        final JSONObject json = new JSONObject(message.getBody());
                        final String jsonArray = json.getString("data");

                        updating = true;
                        runInForeground(new Runnable() {
                            public void run() {
                                try {
                                    if (!persistingToServer) {
                                        int newRevision = json.getInt("currentRevision"); 
                                        if (currentRevision < newRevision) {
                                            logger.debug("CurrentRevision = " + currentRevision + " JSON:");
                                            logger.debug(jsonArray);
                                            currentRevision = newRevision;
                                            jsonDecoder.decode(jsonArray);
                                        }
                                    }
                                } catch (SPPersistenceException e) {
                                    logger.error("Update from server failed!", e);
                                    //logger.warn(jsonArray);
                                    throw new RuntimeException("Please hit the refresh button that does not exist", e);
                                    // TODO discard session and reload
                                } catch (JSONException e) {
                                    logger.error("Update from server failed!", e);
                                } finally {
                                    updating = false;
                                    synchronized (updater) {
                                        updater.notify();
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
	}
	
	private class DataSourceCollectionUpdater implements DatabaseListChangeListener, PropertyChangeListener {
    	
    	/**
    	 * If true this updater is currently posting properties to the server. If
    	 * properties are being posted to the server and an event comes in because
    	 * of a change during posting the updater should not try to repost the message
    	 * it is currently trying to post.
    	 */
    	private boolean postingProperties = false;

        public void attach(DataSourceCollection<JDBCDataSource> dsCollection) {
            dsCollection.addDatabaseListChangeListener(this);
            for (SPDataSource ds : dsCollection.getConnections()) {
                ds.addPropertyChangeListener(this);
            }
        }
        
        public void detach(DataSourceCollection<JDBCDataSource> dsCollection) {
            dsCollection.removeDatabaseListChangeListener(this);
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
            SPDataSource newDS = e.getDataSource();
            newDS.addPropertyChangeListener(this);
            
            List<NameValuePair> properties = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> ent : newDS.getPropertiesMap().entrySet()) {
                properties.add(new BasicNameValuePair(ent.getKey(), ent.getValue()));
            }
            
            postPropertiesToServer(newDS, properties);
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
            SPDataSource ds = (SPDataSource) evt.getSource();
            ds.addPropertyChangeListener(this);
            
            // Updating all properties is less than ideal, but a property change event does
            // not tell us what the "pl.ini" key for the property is.
            List<NameValuePair> properties = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> ent : ds.getPropertiesMap().entrySet()) {
                properties.add(new BasicNameValuePair(ent.getKey(), ent.getValue()));
            }
            
            postPropertiesToServer(ds, properties);
        }

        /**
         * Modifies the properties of the given data source on the server. If
         * the given data source does not exist on the server, it will be
         * created with all of the given properties.
         * 
         * @param ds
         *            The data source to update on the server.
         * @param properties
         *            The properties to update. No properties will be removed
         *            from the server, and only the given properties will be
         *            updated or created.
         */
        private void postPropertiesToServer(SPDataSource ds,
                List<NameValuePair> properties) {
        	if (postingProperties) return;
        	
            HttpClient httpClient = createHttpClient(projectLocation.getServiceInfo());
            try {
            	final ResponseHandler<Void> responseHandler = new ResponseHandler<Void>() {
            		public Void handleResponse(HttpResponse response)
            		throws ClientProtocolException, IOException {
            			
            			if (response.getStatusLine().getStatusCode() != 200) {
            				throw new ClientProtocolException(
            						"Failed to create/update data source on server. Reason:\n" +
            						EntityUtils.toString(response.getEntity()));
            			} else {
            				// success!
            				return null;
            			}
            			
            		}
            	};
            	
                HttpPost request = new HttpPost(dataSourceURI(ds));
                
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
                
                HttpDelete request = new HttpDelete(dataSourceURI(removedDS));
                
                final ResponseHandler<Void> responseHandler = new ResponseHandler<Void>() {
                    public Void handleResponse(HttpResponse response)
                            throws ClientProtocolException, IOException {
                        
                        if (response.getStatusLine().getStatusCode() != 200) {
                            throw new ClientProtocolException(
                                    "Failed to delete data source on server. Reason:\n" +
                                    EntityUtils.toString(response.getEntity()));
                        } else {
                            // success!
                            return null;
                        }
                        
                    }
                };
				httpClient.execute(request, responseHandler);
				
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
        
        /**
         * Returns the URI that references the given data source on the server.
         * 
         * @param ds
         *            The data source whose server URI to return.
         * @return An absolute URI for the given data source on this session's
         *         Architect server.
         */
        private URI dataSourceURI(SPDataSource ds) throws URISyntaxException {
            String type;
            if (ds instanceof JDBCDataSource) {
                type = "jdbc";
            } else if (ds instanceof Olap4jDataSource) {
                type = "olap4j";
            } else {
                throw new UnsupportedOperationException(
                        "Data source type " + ds.getClass() + " is not known");
            }
            
            return getServerURI(projectLocation.getServiceInfo(),
                    "data-sources/" + type + "/" + ds.getName());
        }
    }

	private static class JSONMessage {
	    
	    private final String message;
	    private final boolean successful;
	    
	    public JSONMessage(String message, boolean successful) {
	        this.message = message;
	        this.successful = successful;
	    }
	    
	    public String getBody() {
	        return message;
	    }
	    
	    public boolean isSuccessful() {
	        return successful;
	    }
	}
	
	private static class JSONResponseHandler implements ResponseHandler<JSONMessage> {

	    /*
	     * Unsuccessful responses should have information sent in a header, 
	     * either as "unsuccessfulResponse" or "exceptionStackTrace"
	     */

	    public JSONMessage handleResponse(HttpResponse response) {
	        try {

	            if (response.getStatusLine().getStatusCode() / 100 == 2) {
	                
	                BufferedReader reader = new BufferedReader(
	                        new InputStreamReader(response.getEntity().getContent()));
	                StringBuffer buffer = new StringBuffer();

	                String line;
	                while ((line = reader.readLine()) != null) {
	                    buffer.append(line).append("\n");
	                }

	                logger.debug(buffer);
	                return new JSONMessage(buffer.toString(), true);

	            } else if (response.getFirstHeader("unsuccessfulResponse") != null) {
	                
	                String message = response.getFirstHeader("unsuccessfulResponse").getValue();
	                return new JSONMessage(message, false);
	                
	            } else if (response.getFirstHeader("exceptionStackTrace") != null) {
	                
	                String message = response.getFirstHeader("exceptionStackTrace").getValue();
	                JSONArray stackTraceStrings = new JSONArray(message);
	                StringBuffer stackTraceMessage = new StringBuffer();
	                for (int i = 0; i < stackTraceStrings.length(); i++) {
	                    stackTraceMessage.append("\n").append(stackTraceStrings.get(i));
	                }
	                throw new Exception(stackTraceMessage.toString());
	                
	            } else {
	                throw new RuntimeException("Unable to parse unsuccessful/exception response");
	            }
	        } catch (Exception ex) {
	            throw new RuntimeException(ex);
            }
	    }
	}    
}