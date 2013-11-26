/*
 * Copyright (c) 2010, SQL Power Group Inc.
 */

package ca.sqlpower.architect.enterprise;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import ca.sqlpower.enterprise.ClientSideSessionUtils;
import ca.sqlpower.enterprise.DataSourceCollectionUpdater;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;

public class ArchitectDataSourceCollectionUpdater extends DataSourceCollectionUpdater {
	
	private static Logger logger = Logger.getLogger(ArchitectDataSourceCollectionUpdater.class);
	
	public ArchitectDataSourceCollectionUpdater(ProjectLocation projectLocation) {
		super(projectLocation);
	}
	
	@Override
	public HttpClient getHttpClient() {
		return ArchitectClientSideSession.createHttpClient(projectLocation.getServiceInfo());
	}
    
	@Override
    public void databaseAdded(DatabaseListChangeEvent e) {
        SPDataSource source = e.getDataSource();
        source.addPropertyChangeListener(this);
        
        List<NameValuePair> properties = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> ent : source.getPropertiesMap().entrySet()) {
            properties.add(new BasicNameValuePair(ent.getKey(), ent.getValue()));
        }
        
        if (source instanceof Olap4jDataSource) {
            postOlapDataSourceProperties((Olap4jDataSource) source, properties);
        }
        
        super.databaseAdded(e, source, properties);
    }
    
    private void postOlapDataSourceProperties(Olap4jDataSource ods,
            List<NameValuePair> properties) {
        if (postingProperties) return;
        
        HttpClient httpClient = ArchitectClientSideSession.createHttpClient(projectLocation.getServiceInfo());
        try {
            File schemaFile = new File(ods.getMondrianSchema());
            
            if (!schemaFile.exists()) 
                logger.error("Schema file " + schemaFile.getAbsolutePath() + 
                        " does not exist for data source " + ods.getName());
            
            HttpPost request = new HttpPost(
                    ClientSideSessionUtils.getServerURI(projectLocation.getServiceInfo(), 
                            ArchitectClientSideSession.MONDRIAN_SCHEMA_REL_PATH + schemaFile.getName()));
            
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
    
    private URI olapDataSourceURI(SPDataSource jds) throws URISyntaxException {
        if (!(jds instanceof Olap4jDataSource)) throw new IllegalStateException("DataSource must be an instance of JDBCDataSource");
        
        return ClientSideSessionUtils.getServerURI(projectLocation.getServiceInfo(),
                "/" + ClientSideSessionUtils.REST_TAG + "/data-sources/Olap4jDataSource/" + jds.getName());
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
            
            if (ds instanceof Olap4jDataSource) {
                postOlapDataSourceProperties((Olap4jDataSource) ds, properties);
            }
            
            super.propertyChange(evt, ds, properties);
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
}
