package ca.sqlpower.architect.enterprise;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
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
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.dao.HttpMessageSender;
import ca.sqlpower.dao.MessageSender;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersisterListener;
import ca.sqlpower.dao.SPSessionPersister;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.dao.json.SPJSONPersister;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
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
	
	private final ProjectLocation projectLocation;
	private final HttpClient outboundHttpClient;
	private final SPSessionPersister sessionPersister;
	private final SPJSONPersister jsonPersister;
	private final Updater updater;
	private final DataSourceCollectionUpdater dataSourceCollectionUpdater = new DataSourceCollectionUpdater();
	
	private DataSourceCollection <JDBCDataSource> dataSourceCollection;
	
	// -
	
	int currentRevision = 0;
	
	public ArchitectClientSideSession(ArchitectSessionContext context, 
			String name, ProjectLocation projectLocation) throws SQLObjectException {
		super(context, name);
		
		this.projectLocation = projectLocation;
		
		outboundHttpClient = createHttpClient(projectLocation.getServiceInfo());
		
		getWorkspace().setUUID(projectLocation.getUUID());
		getWorkspace().setName(projectLocation.getName());
		
		MessageSender <JSONObject> messageSender = new Sender(outboundHttpClient, projectLocation.getServiceInfo(), projectLocation.getUUID());
		jsonPersister = new SPJSONPersister(messageSender);
		
		dataSourceCollection = getDataSourceCollection();
		
		sessionPersister = new SPSessionPersister("inbound-" + projectLocation.getUUID(), getWorkspace(), 
				new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		sessionPersister.setSession(this);
		
		updater = new Updater(projectLocation.getUUID(), new SPJSONMessageDecoder(sessionPersister));
	}

	// -
	
	@Override
    public boolean close() {
    	logger.debug("Closing Client Session");
    	try {
    		//TODO: Figure out how to de-register the session &c.
    		//HttpUriRequest request = new HttpDelete(getServerURI(projectLocation.getServiceInfo(), 
    		//		"session/" + getWorkspace().getUUID()));
			//outboundHttpClient.execute(request, new BasicResponseHandler());
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
		
		final SPPersisterListener listener = new SPPersisterListener(jsonPersister,
						new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace().getRootObject()));
		
		SQLPowerUtils.listenToHierarchy(getWorkspace().getRootObject(), listener);
		
		addSessionLifecycleListener(new SessionLifecycleListener<ArchitectSession>() {
			public void sessionClosing(SessionLifecycleEvent<ArchitectSession> e) {
				SQLPowerUtils.unlistenToHierarchy(getWorkspace(), listener);
			}
		});
	}
	
	public void persistProjectToServer() throws SPPersistenceException {
		final SPPersisterListener tempListener = new SPPersisterListener(jsonPersister,
						new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		tempListener.persistObject(getWorkspace().getRootObject(), 0);
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
    		String responseBody = httpClient.execute(request, new BasicResponseHandler());
    		List<ProjectLocation> workspaces = new ArrayList<ProjectLocation>();
    		JSONArray response = new JSONArray(responseBody);
    		logger.debug("Workspace list:\n" + responseBody);
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
	
	public static ProjectLocation createNewServerSession(SPServerInfo serviceInfo) throws URISyntaxException, ClientProtocolException, IOException, JSONException {
    	HttpClient httpClient = createHttpClient(serviceInfo);
    	try {
    		HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, "/jcr/projects/new"));
    		
    		String responseBody = httpClient.execute(request, new BasicResponseHandler());
    		JSONObject response = new JSONObject(responseBody);
    		logger.debug("New Workspace:" + responseBody);
    		return new ProjectLocation(
    					response.getString("uuid"),
    					response.getString("name"),
    					serviceInfo);
    	} finally {
    		httpClient.getConnectionManager().shutdown();
    	}
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
                    "/project/" + projectLocation.getUUID() + 
                    "/revert?revisionNo=" + revisionNo, 
                    new BasicResponseHandler());            
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
        HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, contextRelativePath));
        return httpClient.execute(request, responseHandler);
    }
	
	private static URI getServerURI(SPServerInfo serviceInfo, String contextRelativePath) throws URISyntaxException {
        logger.debug("Getting server URI for: " + serviceInfo);
        String contextPath = serviceInfo.getPath();
        URI serverURI = new URI("http", null, serviceInfo.getServerAddress(), serviceInfo.getPort(),
                contextPath + contextRelativePath, null, null);
        logger.debug("Created URI " + serverURI);
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
	
	public static ArchitectClientSideSession openServerSession(ArchitectSessionContext context, ProjectLocation projectLoc) 
	throws SQLObjectException {
    	final ArchitectClientSideSession session = new ArchitectClientSideSession(context, "", projectLoc);
    	// TODO
    	//context.registerChildSession(session);
		//session.startUpdaterThread();
		return session;
    }
	
	public static List<ArchitectClientSideSession> openServerSessions(ArchitectSessionContext context, SPServerInfo serverInfo) 
	throws IOException, URISyntaxException, JSONException, SQLObjectException {
		List<ArchitectClientSideSession> openedSessions = new ArrayList<ArchitectClientSideSession>();
		for (ProjectLocation workspaceLoc : ArchitectClientSideSession.getWorkspaceNames(serverInfo)) {
			openedSessions.add(openServerSession(context, workspaceLoc));
		}
        return openedSessions;
    }
	
	// Contained classes --------------------------------------------------------------------
	
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

		public void flush() throws SPPersistenceException {
			try {

			    currentRevision++;
			    
				URI serverURI = getServerURI();
				HttpPost postRequest = new HttpPost(serverURI);
				postRequest.setEntity(new StringEntity(message.toString()));
				postRequest.setHeader("Content-Type", "application/json");
				HttpUriRequest request = postRequest;
		        getHttpClient().execute(request, new ResponseHandler<Void>() {
					public Void handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
						StatusLine statusLine = response.getStatusLine();
						if (statusLine.getStatusCode() >= 400) {
						    
						    currentRevision--;
						    
						    throw new ClientProtocolException( 
									"HTTP Post request returned an error: " +
									"Code = " + statusLine.getStatusCode() + ", " +
									"Reason = " + statusLine.getReasonPhrase());
						}
						return null;
					}
		        });
			} catch (ClientProtocolException e) {
			    currentRevision--;
				throw new SPPersistenceException(null, e);
			} catch (IOException e) {
			    currentRevision--;
				throw new SPPersistenceException(null, e);
			} catch (URISyntaxException e) {
			    currentRevision--;
				throw new SPPersistenceException(null, e);
			} finally {
				clear();
			}
		}

		public void send(JSONObject content) throws SPPersistenceException {
			message.put(content);
		}
		
		public URI getServerURI() throws URISyntaxException {
			String contextPath = getServerInfo().getPath();
	        return new URI("http", null, getServerInfo().getServerAddress(), getServerInfo().getPort(),
	                contextPath + "/project/" + getProjectLocation().getUUID(), null, null);
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
			logger.debug("Updater Thread interrupt sent");
			super.interrupt();
			cancelled = true;
		}
        
		@Override
		public void run() {
			logger.info("Updater thread starting");
			
			// the path to contact on the server for update events
			final String contextRelativePath = "/project/" + projectLocation.getUUID();
			
			try {
				while (!this.isInterrupted() && !cancelled) {
					try {
 
					    URI uri = new URI("http", null, projectLocation.getServiceInfo().getServerAddress(), projectLocation.getServiceInfo().getPort(),
				                projectLocation.getServiceInfo().getPath() + contextRelativePath, "oldRevisionNo=" + currentRevision, null);
					    HttpUriRequest request = new HttpGet(uri);
			       
					    String message = inboundHttpClient.execute(request, new BasicResponseHandler());
					    JSONObject json = new JSONObject(message);
					    final String jsonArray = json.getString("data");
					    
					    currentRevision = json.getInt("currentRevision");

					    runInForeground(new Runnable() {
							public void run() {
								try {

									jsonDecoder.decode(jsonArray);
									
								} catch (SPPersistenceException e) {
									logger.error("Update from server failed!", e);
									createUserPrompter(
											"Architect failed to apply an update that was just received from the Enterprise Server.\n"
											+ "The error was:"
											+ "\n" + e.getMessage(),
											UserPromptType.MESSAGE, UserPromptOptions.OK,
											UserPromptResponse.OK, UserPromptResponse.OK, "OK");
									// TODO discard session and reload
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
            	
            	if (ds instanceof Olap4jDataSource 
            			&& ((Olap4jDataSource) ds).getMondrianSchema() != null
            			&& ((Olap4jDataSource) ds).getMondrianSchema().getScheme().equals("file")) {
            		//Pushing the mondrian schema to the server and updating the schema location to a server schema
            		Olap4jDataSource olapDS = ((Olap4jDataSource) ds);
            		File schemaFile = new File(olapDS.getMondrianSchema());
            		
            		if (!schemaFile.exists()) 
            			logger.error("Schema file " + schemaFile.getAbsolutePath() + 
            					" does not exist for data source " + ds.getName());
            		
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
            			olapDS.setMondrianSchema(new URI(SPDataSource.SERVER + schemaFile.getName()));
            		} finally {
            			postingProperties = false;
            		}
            	}
                
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
				
				if (removedDS instanceof Olap4jDataSource
						&& ((Olap4jDataSource) removedDS).getMondrianSchema() != null) {
					URI serverURI = ((Olap4jDataSource) removedDS).getMondrianSchema();
					logger.debug("Server URI for deletion is " + serverURI);
					HttpDelete schemaRequest = new HttpDelete(serverURI);
					httpClient.execute(schemaRequest, responseHandler);
				}
                
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
    
}
