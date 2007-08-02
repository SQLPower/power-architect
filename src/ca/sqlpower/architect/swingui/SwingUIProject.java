/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLExceptionNode;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.SQLTable.Folder;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileManager;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.CompareDMSettings.SourceOrTargetSettings;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.xml.UnescapingSaxParser;
import ca.sqlpower.xml.XMLHelper;

/**
 * The SwingUIProject class is a container that holds all information pertaining
 * to a particular project.  It is capable of writing itself to an output stream,
 * and reading in a previously-written stream to re-create a previous instance of
 * a project at a later date.  Such "project files" are meant to be highly portable,
 * and should remain backward compatible now that the product has been released.
 *
 * <p>Anyone who makes a change to the file reading code which causes a failure to
 * read older (release 1.0.19 or later) project files will get Airzooka'ed.
 */
public class SwingUIProject {
    private static final Logger logger = Logger.getLogger(SwingUIProject.class);

    //  ---------------- persistent properties -------------------

    private File file;
    
    // ------------------ load and save support -------------------

    /**
     * Tracks whether or not this project has been modified since last saved.
     */
    private boolean modified;

    /**
     * Don't let application exit while saving.
     */
    private boolean saveInProgress;

    /**
     * @return Returns the saveInProgress.
     */
    public boolean isSaveInProgress() {
        return saveInProgress;
    }
    /**
     * @param saveInProgress The saveInProgress to set.
     */
    public void setSaveInProgress(boolean saveInProgress) {
        this.saveInProgress = saveInProgress;
    }
    /**
     * Should be set to NULL unless we are currently saving the
     * project, at which time it's writing to the project file.
     */
    private PrintWriter out;

    /**
     * During a LOAD, this map maps String ID codes to SQLObject instances.
     * During a SAVE, it holds mappings from SQLObject instance to String
     * ID (the inverse of the LOAD mapping).
     */
    private Map objectIdMap;

    /**
     * During a LOAD, this map maps String ID codes to DBCS instances.
     * During a SAVE, it holds mappings from DBCS instance to String
     * ID (the inverse of the LOAD mapping).
     */
    private Map dbcsIdMap;

    /**
     * Shows progress during saves and loads.
     */
    private ProgressMonitor pm;

    /**
     * The last value we sent to the progress monitor.
     */
    private int progress = 0;
    
    private final ArchitectSwingSession session;
    
    /**
     * Sets up a new project file load/save object for the given session.
     * 
     * @param session the session that this instance will be responsible for
     * loading into and saving out from.
     * @throws NullPointerException if the given session is null
     */
    public SwingUIProject(ArchitectSwingSession session) throws ArchitectException {
        if (session == null) {
            throw new NullPointerException("Null session is not allowed!");
        }
        this.session = session;
    }

    // ------------- READING THE PROJECT FILE ---------------

    public void load(InputStream in, DataSourceCollection dataSources) throws IOException, ArchitectException {
        dbcsIdMap = new HashMap();
        objectIdMap = new HashMap();

        Digester digester = null;
        
        // use digester to read from file
        try {
            digester = setupDigester();
            digester.parse(in);
        } catch (SAXException ex) {
            logger.error("SAX Exception in project file parse!", ex);
            String message;
            if (digester == null) {
                message = "Couldn't create an XML parser";
            } else {
                message = "There is an XML parsing error in project file at Line:" + 
                digester.getDocumentLocator().getLineNumber() + " Column:" +
                digester.getDocumentLocator().getColumnNumber();
            }
            throw new ArchitectException(message, ex);
        } catch (IOException ex) {
            logger.error("IO Exception in project file parse!", ex);
            throw new ArchitectException("There was an I/O error while reading the file", ex);
        } catch (Exception ex) {
            logger.error("General Exception in project file parse!", ex);
            throw new ArchitectException("Unexpected Exception", ex);
        }

        SQLObject dbConnectionContainer = ((SQLObject) session.getSourceDatabases().getModel().getRoot());
        dbConnectionContainer.addChild(0, session.getPlayPen().getDatabase());

        // hook up data source parent types
        for (SQLDatabase db : (List<SQLDatabase>) dbConnectionContainer.getChildren()) {
            SPDataSource ds = db.getDataSource();
            String parentTypeId = ds.getPropertiesMap().get(SPDataSource.DBCS_CONNECTION_TYPE);
            if (parentTypeId != null) {
                for (SPDataSourceType dstype : dataSources.getDataSourceTypes()) {
                    if (dstype.getName().equals(parentTypeId)) {
                        ds.setParentType(dstype);
                        // TODO unit test that this works
                    }
                }
                if (ds.getParentType() == null) {
                    logger.error("Data Source \""+ds.getName()+"\" has type \""+parentTypeId+"\", which is not configured in the user prefs.");
                    // TODO either reconstruct the parent type, or bring this problem to the attention of the user.
                    // TODO test this
                } else {
                    // TODO test that the referenced parent type is properly configured (has a driver, etc)
                    // TODO test for this behaviour
                }
            }
            
        }
        
        /*
         * for backward compatibilty, in the old project file, we have
         * primaryKeyName in the table attrbute, but nothing
         * in the sqlIndex that indicates primary key index,
         * so, we have to set the index as primary key index
         * if the index name == table.primaryKeyName after load the project,
         * table.primaryKeyName is save in the map now, not in the table object
         */
        for (SQLTable table : (List<SQLTable>)session.getPlayPen().getDatabase().getTables()) {

            if (logger.isDebugEnabled()) {
                if (!table.isPopulated()) {
                    logger.debug("Table ["+table.getName()+"] not populated");
                } else if (table.getIndicesFolder() == null) {
                    logger.debug("Table ["+table.getName()+"] has null indices folder");
                } else {
                    logger.debug("Table ["+table.getName()+"] index folder contents: "+table.getIndicesFolder().getChildren());
                }
            }
            
            if (table.getIndicesFolder() == null) {
                logger.debug("this must be a very old version, we have to add the index" +
                        " folder manually. the table is [" + table.getName() + "]");
                table.addChild(new Folder(Folder.INDICES, true));
            }
            
            if ( table.getPrimaryKeyIndex() == null) {
                logger.debug("primary key index is null in table: " + table);
                logger.debug("number of children found in indices folder: " + table.getIndicesFolder().getChildCount());
                for (SQLIndex index : (List<SQLIndex>)table.getIndicesFolder().getChildren()) {
                    if (objectIdMap.get(table.getName()+"."+index.getName()) != null) {
                        index.setPrimaryKeyIndex(true);
                        break;
                    }
                }
            }
            logger.debug("Table ["+table.getName()+"]2 index folder contents: "+table.getIndicesFolder().getChildren());
            table.normalizePrimaryKey();
            logger.debug("Table ["+table.getName()+"]3 index folder contents: "+table.getIndicesFolder().getChildren());
            
            if (logger.isDebugEnabled()) {
                if (!table.isPopulated()) {
                    logger.debug("Table ["+table.getName()+"] not populated");
                } else if (table.getIndicesFolder() == null) {
                    logger.debug("Table ["+table.getName()+"] has null indices folder");
                } else {
                    logger.debug("Table ["+table.getName()+"] index folder contents: "+table.getIndicesFolder().getChildren());
                }
            }

        }

        setModified(false);
        // TODO change this to load the undo history from a file
        session.getUndoManager().discardAllEdits();
    }



    private Digester setupDigester() throws ParserConfigurationException, SAXException {
        Digester d = new Digester(new UnescapingSaxParser());
        d.setValidating(false);
        d.push(session);

        // project name
        d.addCallMethod("architect-project/project-name", "setName", 0); // argument is element body text

        // source DB connection specs (deprecated in favour of project-data-sources; this is only here for backward compatibility)
        DBCSFactory dbcsFactory = new DBCSFactory();
        d.addFactoryCreate("architect-project/project-connection-specs/dbcs", dbcsFactory);
        d.addSetProperties
        ("architect-project/project-connection-specs/dbcs",
                new String[] {"connection-name", "driver-class", "jdbc-url", "user-name",
                "user-pass", "sequence-number", "single-login"},
                new String[] {"displayName", "driverClass", "url", "user",
                "pass", "seqNo", "singleLogin"});
        d.addCallMethod("architect-project/project-connection-specs/dbcs", "setName", 0);
        // these instances get picked out of the dbcsIdMap by the SQLDatabase factory

        // project data sources (replaces project connection specs)
        d.addFactoryCreate("architect-project/project-data-sources/data-source", dbcsFactory);
        d.addCallMethod("architect-project/project-data-sources/data-source/property", "put", 2);
        d.addCallParam("architect-project/project-data-sources/data-source/property", 0, "key");
        d.addCallParam("architect-project/project-data-sources/data-source/property", 1, "value");
        // for the project-data-sources, these instances get picked out of the dbcsIdMap by the SQLDatabase factory

        // but for the create kettle job settings, we add them explicitly
        
        
        // source database hierarchy
        d.addObjectCreate("architect-project/source-databases", LinkedList.class);
        d.addSetNext("architect-project/source-databases", "setSourceDatabaseList");

        SQLDatabaseFactory dbFactory = new SQLDatabaseFactory();
        d.addFactoryCreate("architect-project/source-databases/database", dbFactory);
        d.addSetProperties("architect-project/source-databases/database");
        d.addSetNext("architect-project/source-databases/database", "add");

        d.addObjectCreate("architect-project/source-databases/database/catalog", SQLCatalog.class);
        d.addSetProperties("architect-project/source-databases/database/catalog");
        d.addSetNext("architect-project/source-databases/database/catalog", "addChild");

        SQLSchemaFactory schemaFactory = new SQLSchemaFactory();
        d.addFactoryCreate("*/schema", schemaFactory);
        d.addSetProperties("*/schema");
        d.addSetNext("*/schema", "addChild");

        SQLTableFactory tableFactory = new SQLTableFactory();
        d.addFactoryCreate("*/table", tableFactory);
        d.addSetProperties("*/table");
        d.addSetNext("*/table", "addChild");

        SQLFolderFactory folderFactory = new SQLFolderFactory();
        d.addFactoryCreate("*/folder", folderFactory);
        d.addSetProperties("*/folder");
        d.addSetNext("*/folder", "addChild");

        SQLColumnFactory columnFactory = new SQLColumnFactory();
        d.addFactoryCreate("*/column", columnFactory);
        d.addSetProperties("*/column");
        // this needs to be manually set last to prevent generic types
        // from overwriting database specific types

        // Old name (it has been updated to sourceDataTypeName)
        d.addCallMethod("*/column","setSourceDataTypeName",1);
        d.addCallParam("*/column",0,"sourceDBTypeName");

        // new name
        d.addCallMethod("*/column","setSourceDataTypeName",1);
        d.addCallParam("*/column",0,"sourceDataTypeName");
        d.addSetNext("*/column", "addChild");

        SQLRelationshipFactory relationshipFactory = new SQLRelationshipFactory();
        d.addFactoryCreate("*/relationship", relationshipFactory);
        d.addSetProperties("*/relationship");
        // the factory adds the relationships to the correct PK and FK tables

        ColumnMappingFactory columnMappingFactory = new ColumnMappingFactory();
        d.addFactoryCreate("*/column-mapping", columnMappingFactory);
        d.addSetProperties("*/column-mapping");
        d.addSetNext("*/column-mapping", "addChild");

        SQLIndexFactory indexFactory = new SQLIndexFactory();
        d.addFactoryCreate("*/index", indexFactory);
        d.addSetProperties("*/index");
        d.addSetNext("*/index", "addChild");

        SQLIndexColumnFactory indexColumnFactory = new SQLIndexColumnFactory();
        d.addFactoryCreate("*/index-column", indexColumnFactory);
        d.addSetProperties("*/index-column");
        d.addSetNext("*/index-column", "addChild");

        SQLExceptionFactory exceptionFactory = new SQLExceptionFactory();
        d.addFactoryCreate("*/sql-exception", exceptionFactory);
        d.addSetProperties("*/sql-exception");
        d.addSetNext("*/sql-exception", "addChild");

        TargetDBFactory targetDBFactory = new TargetDBFactory();
        // target database hierarchy
        d.addFactoryCreate("architect-project/target-database", targetDBFactory);
        d.addSetProperties("architect-project/target-database");

        // the play pen
        TablePaneFactory tablePaneFactory = new TablePaneFactory();
        d.addFactoryCreate("architect-project/play-pen/table-pane", tablePaneFactory);
        // factory will add the tablepanes to the playpen

        PPRelationshipFactory ppRelationshipFactory = new PPRelationshipFactory();
        d.addFactoryCreate("architect-project/play-pen/table-link", ppRelationshipFactory);

        DDLGeneratorFactory ddlgFactory = new DDLGeneratorFactory();
        d.addFactoryCreate("architect-project/ddl-generator", ddlgFactory);
        d.addSetProperties("architect-project/ddl-generator");

        CreateKettleJobSettingsFactory ckjsFactory = new CreateKettleJobSettingsFactory();
        d.addFactoryCreate("architect-project/create-kettle-job-settings", ckjsFactory);
        d.addSetProperties("architect-project/create-kettle-job-settings");

        CompareDMSettingFactory settingFactory = new CompareDMSettingFactory();
        d.addFactoryCreate("architect-project/compare-dm-settings", settingFactory);
        d.addSetProperties("architect-project/compare-dm-settings");

        CompareDMStuffSettingFactory sourceStuffFactory = new CompareDMStuffSettingFactory(true);
        d.addFactoryCreate("architect-project/compare-dm-settings/source-stuff", sourceStuffFactory);
        d.addSetProperties("architect-project/compare-dm-settings/source-stuff");

        CompareDMStuffSettingFactory targetStuffFactory = new CompareDMStuffSettingFactory(false);
        d.addFactoryCreate("architect-project/compare-dm-settings/target-stuff", targetStuffFactory);
        d.addSetProperties("architect-project/compare-dm-settings/target-stuff");

        ProfileManagerFactory profileManagerFactory = new ProfileManagerFactory();
        d.addFactoryCreate("*/profiles", profileManagerFactory);
        d.addSetProperties("*/profiles");

        ProfileResultFactory profileResultFactory = new ProfileResultFactory();
        d.addFactoryCreate("*/profiles/profile-result", profileResultFactory);
        d.addSetProperties("*/profiles/profile-result");
        d.addSetNext("*/profiles/profile-result", "loadResult");

        ProfileResultValueFactory profileResultValueFactory = new ProfileResultValueFactory();
        d.addFactoryCreate("*/profiles/profile-result/avgValue", profileResultValueFactory );
        d.addSetNext("*/profiles/profile-result/avgValue", "setAvgValue");
        d.addFactoryCreate("*/profiles/profile-result/minValue", profileResultValueFactory);
        d.addSetNext("*/profiles/profile-result/minValue", "setMinValue");
        d.addFactoryCreate("*/profiles/profile-result/maxValue", profileResultValueFactory);
        d.addSetNext("*/profiles/profile-result/maxValue", "setMaxValue");

        ProfileResultTopNValueFactory topNValueFactory = new ProfileResultTopNValueFactory();
        d.addFactoryCreate("*/profiles/profile-result/topNvalue", topNValueFactory );
        d.addSetNext("*/profiles/profile-result/topNvalue", "addValueCount");

        FileFactory fileFactory = new FileFactory();
        d.addFactoryCreate("*/file", fileFactory);
        d.addSetNext("*/file", "setFile");

        d.addSetNext("architect-project/ddl-generator", "setDDLGenerator");

        return d;
    }

    /**
     * Creates a SPDataSource object and puts a mapping from its
     * id (in the attributes) to the new instance into the dbcsIdMap.
     */
    private class DBCSFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            SPDataSource dbcs = new SPDataSource(session.getContext().getUserSettings().getPlDotIni());
            
            String id = attributes.getValue("id");
            if (id != null) {
                dbcsIdMap.put(id, dbcs);
            } else {
                logger.info("No ID found in dbcs element while loading project! (this is normal for playpen db, but bad for other data sources!");
            }
            return dbcs;
        }
    }
    /**
     * Gets the playpen SQLDatabase instance.
     * Also attaches the DBCS referenced by the dbcsref attribute, if
     * there is such an attribute.
     * NOTE: this will only work until we support multiple playpens.
     */
    private class TargetDBFactory extends AbstractObjectCreationFactory {

        @Override
        public Object createObject(Attributes attributes) throws Exception {
            SQLDatabase ppdb = session.getPlayPen().getDatabase();

            String dbcsid = attributes.getValue("dbcs-ref");
            if (dbcsid != null) {
                ppdb.setDataSource((SPDataSource) dbcsIdMap.get(dbcsid));
            }
            return ppdb;
        }

    }


    /**
     * Creates a SQLDatabase instance and adds it to the objectIdMap.
     * Also attaches the DBCS referenced by the dbcsref attribute, if
     * there is such an attribute.
     */
    private class SQLDatabaseFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            SQLDatabase db = new SQLDatabase();

            String id = attributes.getValue("id");
            if (id != null) {
                objectIdMap.put(id, db);
            } else {
                logger.warn("No ID found in database element while loading project!");
            }

            String dbcsid = attributes.getValue("dbcs-ref");
            if (dbcsid != null) {
                db.setDataSource((SPDataSource) dbcsIdMap.get(dbcsid));
            }

            String populated = attributes.getValue("populated");
            if (populated != null && populated.equals("false")) {
                db.setPopulated(false);
            }

            return db;
        }
    }

    /**
     * Creates a SQLSchema instance and adds it to the objectIdMap.
     */
    private class SQLSchemaFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            boolean startPopulated;
            String populated = attributes.getValue("populated");
            startPopulated = (populated != null && populated.equals("true"));

            SQLSchema schema = new SQLSchema(startPopulated);
            String id = attributes.getValue("id");
            if (id != null) {
                objectIdMap.put(id, schema);
            } else {
                logger.warn("No ID found in database element while loading project!");
            }

            return schema;
        }
    }

    /**
     * Creates a SQLTable instance and adds it to the objectIdMap.
     */
    private class SQLTableFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) throws ArchitectException{
            SQLTable tab = new SQLTable();

            String id = attributes.getValue("id");
            String pkName = attributes.getValue("primaryKeyName");

            if (id != null) {
                objectIdMap.put(id, tab);
                objectIdMap.put(id+"."+pkName, tab);
            } else {
                logger.warn("No ID found in table element while loading project!");
            }

            String populated = attributes.getValue("populated");
            if (populated != null && populated.equals("false")) {
                try {
                    tab.initFolders(false);
                } catch (ArchitectException e) {
                    logger.error("Couldn't add folder to table \""+tab.getName()+"\"", e);
                    JOptionPane.showMessageDialog(null, "Failed to add folder to table:\n"+e.getMessage());
                }
            }

            return tab;
        }
    }

    /**
     * Creates a SQLFolder instance which is marked as populated.
     */
    private class SQLFolderFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            int type = -1;
            String typeStr = attributes.getValue("type");
            if (typeStr == null) {
                // backward compatibility: derive type from name
                String name = attributes.getValue("name");
                if (name.equals("Columns")) type = SQLTable.Folder.COLUMNS;
                else if (name.equals("Imported Keys")) type = SQLTable.Folder.IMPORTED_KEYS;
                else if (name.equals("Exported Keys")) type = SQLTable.Folder.EXPORTED_KEYS;
                else throw new IllegalStateException("Could not determine folder type from name");
            } else {
                try {
                    type = Integer.parseInt(typeStr);
                } catch (NumberFormatException ex) {
                    throw new IllegalStateException("Could not parse folder type id \""
                            +typeStr+"\"");
                }
            }
            return new SQLTable.Folder(type, true);
        }
    }

    /**
     * Creates a SQLColumn instance and adds it to the
     * objectIdMap. Also dereferences the source-column-ref attribute
     * if present.
     */
    private class SQLColumnFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            SQLColumn col = new SQLColumn();

            String id = attributes.getValue("id");
            if (id != null) {
                objectIdMap.put(id, col);
            } else {
                logger.warn("No ID found in column element while loading project!");
            }

            String sourceId = attributes.getValue("source-column-ref");
            if (sourceId != null) {
                col.setSourceColumn((SQLColumn) objectIdMap.get(sourceId));
            }

            return col;
        }
    }

    /**
     * Creates a SQLException instance and adds it to the
     * objectIdMap.
     */
    private class SQLExceptionFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            SQLExceptionNode exc = new SQLExceptionNode(null, null);

            String id = attributes.getValue("id");
            if (id != null) {
                objectIdMap.put(id, exc);
            } else {
                logger.warn("No ID found in exception element while loading project!");
            }

            exc.setMessage(attributes.getValue("message"));

            return exc;
        }
    }

    /**
     * Creates a SQLRelationship instance and adds it to the
     * objectIdMap.  Also dereferences the fk-table-ref and
     * pk-table-ref attributes if present.
     */
    private class SQLRelationshipFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            SQLRelationship rel = new SQLRelationship();

            String id = attributes.getValue("id");
            if (id != null) {
                objectIdMap.put(id, rel);
            } else {
                logger.warn("No ID found in relationship element while loading project!");
            }

            String fkTableId = attributes.getValue("fk-table-ref");
            String pkTableId = attributes.getValue("pk-table-ref");

            if (fkTableId != null && pkTableId != null) {
                SQLTable fkTable = (SQLTable) objectIdMap.get(fkTableId);
                SQLTable pkTable = (SQLTable) objectIdMap.get(pkTableId);
                try {
                    rel.attachRelationship(pkTable, fkTable, false);
                } catch (ArchitectException e) {
                    logger.error("Couldn't attach relationship to pktable \""+pkTable.getName()+"\" and fktable \""+fkTable.getName()+"\"", e);
                    JOptionPane.showMessageDialog(null, "Failed to attach relationship to pktable \""+pkTable.getName()+"\" and fktable \""+fkTable.getName()+"\":\n"+e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(null, "Missing pktable or fktable references for relationship id \""+id+"\"");
            }

            return rel;
        }
    }

    /**
     * Creates a ColumnMapping instance and adds it to the
     * objectIdMap.  Also dereferences the fk-column-ref and
     * pk-column-ref attributes if present.
     */
    private class ColumnMappingFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            SQLRelationship.ColumnMapping cmap = new SQLRelationship.ColumnMapping();

            String id = attributes.getValue("id");
            if (id != null) {
                objectIdMap.put(id, cmap);
            } else {
                logger.warn("No ID found in column-mapping element while loading project!");
            }

            String fkColumnId = attributes.getValue("fk-column-ref");
            if (fkColumnId != null) {
                cmap.setFkColumn((SQLColumn) objectIdMap.get(fkColumnId));
            }

            String pkColumnId = attributes.getValue("pk-column-ref");
            if (pkColumnId != null) {
                cmap.setPkColumn((SQLColumn) objectIdMap.get(pkColumnId));
            }

            return cmap;
        }
    }

    /**
     * The index most recently loaded from the project file.  The SQLIndexColumnFactory
     * has to know which index owns the index column in order to create it.
     */
    private SQLIndex currentIndex;

    /**
     * Creates a SQLIndex instance and adds it to the objectIdMap.
     */
    private class SQLIndexFactory extends AbstractObjectCreationFactory {

        public Object createObject(Attributes attributes) {
            SQLIndex index = new SQLIndex();
            logger.debug("Loading index: "+attributes.getValue("name"));
            String id = attributes.getValue("id");
            if (id != null) {
                objectIdMap.put(id, index);
            } else {
                logger.warn("No ID found in index element while loading project!");
            }
            for (int i = 0; i < attributes.getLength(); i++) {
                logger.debug("Attribute: \"" + attributes.getQName(i) + "\" Value:"+attributes.getValue(i));
            }
            index.setType(SQLIndex.IndexType.valueOf(attributes.getValue("index-type")));
    
            currentIndex = index;
            return index;
        }
    }

    /**
     * Creates a SQLIndex instance and adds it to the
     * objectIdMap.  Also dereferences the column-ref if present.
     */
    private class SQLIndexColumnFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Column col = currentIndex.new Column();

            String id = attributes.getValue("id");
            if (id != null) {
                objectIdMap.put(id, col);
            } else {
                logger.warn("No ID found in index-column element while loading project!");
            }

            String referencedColId = attributes.getValue("column-ref");
            if (referencedColId != null) {
                SQLColumn column = (SQLColumn) objectIdMap.get(referencedColId);
                col.setColumn(column);
            }

            return col;
        }
    }

    private class TablePaneFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            int x = Integer.parseInt(attributes.getValue("x"));
            int y = Integer.parseInt(attributes.getValue("y"));
            SQLTable tab = (SQLTable) objectIdMap.get(attributes.getValue("table-ref"));
            TablePane tp = new TablePane(tab, session.getPlayPen());
            session.getPlayPen().addTablePane(tp, new Point(x, y));
            return tp;
        }
    }

    private class PPRelationshipFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Relationship r = null;
            try {
                SQLRelationship rel =
                    (SQLRelationship) objectIdMap.get(attributes.getValue("relationship-ref"));
                r = new Relationship(session.getPlayPen(), rel);
                session.getPlayPen().addRelationship(r);

                int pkx = Integer.parseInt(attributes.getValue("pk-x"));
                int pky = Integer.parseInt(attributes.getValue("pk-y"));
                int fkx = Integer.parseInt(attributes.getValue("fk-x"));
                int fky = Integer.parseInt(attributes.getValue("fk-y"));
                r.setPkConnectionPoint(new Point(pkx, pky));
                r.setFkConnectionPoint(new Point(fkx, fky));
            } catch (ArchitectException e) {
                logger.error("Couldn't create relationship component", e);
            } catch (NumberFormatException e) {
                logger.warn("Didn't set connection points because of integer parse error");
            } catch (NullPointerException e) {
                logger.debug("No pk/fk connection points specified in save file;"
                        +" not setting custom connection points");
            }
            return r;
        }
    }

    private class DDLGeneratorFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) throws SQLException {
            try {
                GenericDDLGenerator ddlg =
                    (GenericDDLGenerator) Class.forName(attributes.getValue("type")).newInstance();
                ddlg.setTargetCatalog(attributes.getValue("target-catalog"));
                ddlg.setTargetSchema(attributes.getValue("target-schema"));
                return ddlg;
            } catch (Exception e) {
                logger.debug("Couldn't create DDL Generator instance. Returning generic instance.", e);
                return new GenericDDLGenerator();
            }
        }
    }

    private class CreateKettleJobSettingsFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) throws SQLException {
            return session.getCreateKettleJob();
        }
    }

    private class FileFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            return new File(attributes.getValue("path"));
        }
    }

    /**
     * Creates a compareDM instance and adds it to the objectIdMap.
     */
    private class CompareDMSettingFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {

            return session.getCompareDMSettings();
        }
    }

    private class CompareDMStuffSettingFactory extends AbstractObjectCreationFactory {
        private boolean source;
        public CompareDMStuffSettingFactory(boolean source) {
            this.source = source;
        }
        public Object createObject(Attributes attributes) {
            if ( source )
                return session.getCompareDMSettings().getSourceSettings();
            else
                return session.getCompareDMSettings().getTargetSettings();
        }
    }

    /**
     * Just returns the existing profile manager (this way, all the profile results
     * will get added to the existing one)
     */
    private class ProfileManagerFactory extends AbstractObjectCreationFactory {
        @Override
        public Object createObject(Attributes attributes) throws ArchitectException {
            return session.getProfileManager();
        }
    }

    private class ProfileResultFactory extends AbstractObjectCreationFactory {

        /**
         * The most recent table result encountered.
         */
        TableProfileResult tableProfileResult;
    
        @Override
        public Object createObject(Attributes attributes) throws ArchitectException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            String refid = attributes.getValue("ref-id");
            String className = attributes.getValue("type");
            
            if (refid == null) {
                throw new ArchitectException("Missing mandatory attribute \"ref-id\" in <profile-result> element");
            }

            if (className == null) {
                throw new ArchitectException("Missing mandatory attribute \"type\" in <profile-result> element");
            } else if (className.equals(TableProfileResult.class.getName())) {
                SQLTable t = (SQLTable) objectIdMap.get(refid);
                
                // XXX we should actually store the settings together with each profile result, not rehash the current defaults
                tableProfileResult = new TableProfileResult(t, session.getProfileManager(), session.getProfileManager().getDefaultProfileSettings());
                
                tableProfileResult.finish(tableProfileResult.getCreateEndTime());
                return tableProfileResult;
            } else if (className.equals(ColumnProfileResult.class.getName())) {
                SQLColumn c = (SQLColumn) objectIdMap.get(refid);
                if (tableProfileResult == null) {
                    throw new IllegalArgumentException("Column result does not have a parent");
                }
                ColumnProfileResult cpr = new ColumnProfileResult(c, tableProfileResult);
                tableProfileResult.addColumnProfileResult(cpr);
                return cpr;
            } else {
                throw new ArchitectException("Profile result type \""+className+"\" not recognised");
            }
        }
    }

    private class ProfileResultValueFactory extends AbstractObjectCreationFactory {
        @Override
        public Object createObject(Attributes attributes) throws ArchitectException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            String className = attributes.getValue("type");
            if (className == null) {
                throw new ArchitectException("Missing mandatory attribute \"type\" in <avgValue> or <minValue> or <maxValue> element");
            } else if (className.equals(BigDecimal.class.getName()) ) {
                return new BigDecimal(attributes.getValue("value"));
            } else if (className.equals(Timestamp.class.getName()) ) {
                return new Timestamp( Timestamp.valueOf(attributes.getValue("value")).getTime() );
            } else if (className.equals(String.class.getName()) ) {
                return new String(attributes.getValue("value"));
            } else {
                return new String(attributes.getValue("value"));
            }
        }
    }

    private class ProfileResultTopNValueFactory extends AbstractObjectCreationFactory {
        @Override
        public Object createObject(Attributes attributes) throws ArchitectException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            String className = attributes.getValue("type");
            int count = Integer.valueOf(attributes.getValue("count"));
            String value = attributes.getValue("value");

            if (className == null || className.length() == 0 ) {
                return new ColumnValueCount(null,count);
            } else if (className.equals(BigDecimal.class.getName()) ) {
                return new ColumnValueCount(new BigDecimal(value),count);
            } else if (className.equals(Timestamp.class.getName()) ) {
                return new ColumnValueCount(new Timestamp( Timestamp.valueOf(value).getTime() ),count);
            } else if (className.equals(String.class.getName()) ) {
                return new ColumnValueCount(new String(value),count);
            } else {
                return new ColumnValueCount(new String(value),count);
            }
        }
    }

    // ------------- WRITING THE PROJECT FILE ---------------

    /**
     * Saves this project by writing an XML description of it to a temp file, then renaming.
     * The location of the file is determined by this project's <code>file</code> property.
     *
     * @param pm An optional progress monitor which will be initialised then updated
     * periodically during the save operation.  If you use a progress monitor, don't
     * invoke this method on the AWT event dispatch thread!
     */
    public void save(ProgressMonitor pm) throws IOException, ArchitectException {
        // write to temp file and then rename (this preserves old project file
        // when there's problems)
        if (file.exists() && !file.canWrite()) {
            // write problems with architect file will muck up the save process
            throw new ArchitectException("problem saving project -- "
                    + "cannot write to architect file: "
                    + file.getAbsolutePath());
        }

        File backupFile = new File (file.getParent(), file.getName()+"~");

        // Several places we would check dir perms, but MS-Windows stupidly doesn't let use the
        // "directory write" attribute for directory writing (but instead overloads
        // it to mean 'this is a special directory'.

        File tempFile = null;
        tempFile = new File (file.getParent(),"tmp___" + file.getName());
        String encoding = "UTF-8";
        try {
            // If creating this temp file fails, feed the user back a more explanatory message
            out = new PrintWriter(tempFile,encoding);
        } catch (IOException e) {
            throw new ArchitectException("Unable to create output file for save operation, data NOT saved.\n" + e, e);
        }

        progress = 0;
        this.pm = pm;
        if (pm != null) {
            int pmMax = 0;
            pm.setMinimum(0);
            if (session.isSavingEntireSource()) {
                pmMax = ArchitectUtils.countTablesSnapshot((SQLObject) session.getSourceDatabases().getModel().getRoot());
            } else {
                pmMax = ArchitectUtils.countTables((SQLObject) session.getSourceDatabases().getModel().getRoot());
            }
            logger.error("Setting progress monitor maximum to "+pmMax);
            pm.setMaximum(pmMax);
            pm.setProgress(progress);
            pm.setMillisToDecideToPopup(0);
        }

        save(out,encoding);	// Does ALL the actual I/O
        out = null;
        if (pm != null)
            pm.close();
        pm = null;

        // Do the rename dance.
        // This is a REALLY bad place for failure (especially if we've made the user wait several hours to save
        // a large project), so we MUST check failures from renameto (both places!)
        boolean fstatus = false;
        fstatus = backupFile.delete();
        logger.debug("deleting backup~ file: " + fstatus);

        // If this is a brand new project, the old file does not yet exist, no point trying to rename it.
        // But if it already existed, renaming current to backup must succeed, or we give up.
        if (file.exists()) {
            fstatus = file.renameTo(backupFile);
            logger.debug("rename current file to backupFile: " + fstatus);
            if (!fstatus) {
                throw new ArchitectException((
                        "Could not rename current file to backup\nProject saved in " +
                        tempFile + ": " + file + " still contains old project"));
            }
        }
        fstatus = tempFile.renameTo(file);
        if (!fstatus) {
            throw new ArchitectException((
                    "Could not rename temp file to current\nProject saved in " +
                    tempFile + ": " + file + " still contains old project"));
        }
        logger.debug("rename tempFile to current file: " + fstatus);
    }

    XMLHelper ioo = new XMLHelper();
    
    /**
     * Do just the writing part of save, given a PrintWriter
     * @param out - the file to write to
     * @return True iff the save completed OK
     * @throws IOException
     * @throws ArchitectException
     */
    public void save(PrintWriter out, String encoding) throws IOException, ArchitectException {
        objectIdMap = new IdentityHashMap();
        dbcsIdMap = new HashMap();
        ioo.indent = 0;

        try {
            ioo.println(out, "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>");
            ioo.println(out, "<architect-project version=\"1.0\" appversion=\""+ArchitectVersion.APP_VERSION+"\">");
            ioo.indent++;
            ioo.println(out, "<project-name>"+SQLPowerUtils.escapeXML(session.getName())+"</project-name>");
            saveDataSources(out);
            saveSourceDatabases(out);
            saveTargetDatabase(out);
            saveDDLGenerator(out);
            saveCompareDMSettings(out);
            saveCreateKettleJobSettings(out);
            savePlayPen(out);
            saveProfiles(out);
            ioo.indent--;
            ioo.println(out, "</architect-project>");
            setModified(false);
        } finally {
            if (out != null) out.close();
        }
    }
    
    public void save(OutputStream out, String encoding) throws IOException, ArchitectException {
        save(new PrintWriter(new OutputStreamWriter(out, encoding)), encoding);
    }

    private void saveDataSources(PrintWriter out) throws IOException, ArchitectException {
        // FIXME this needs work.  It should include everything we need in order to build
        //       the referenced parent type from scratch (except the jdbc driver path)
        //       and the code that loads a project should check if the referenced parent
        //       type exists.  If not, we need to create everything we can about the parent
        //       type, then show the driver manager gui and get the user to pick a jdbc driver file.
        ioo.println(out, "<project-data-sources>");
        ioo.indent++;
        int dsNum = 0;
        SQLObject dbTreeRoot = (SQLObject) session.getSourceDatabases().getModel().getRoot();
        Iterator it = dbTreeRoot.getChildren().iterator();
        while (it.hasNext()) {
            SQLObject o = (SQLObject) it.next();
            SPDataSource ds = ((SQLDatabase) o).getDataSource();
            if (ds != null) {
                String id = (String) dbcsIdMap.get(ds);
                if (id == null) {
                    id = "DS"+dsNum;
                    dbcsIdMap.put(ds, id);
                }
                ioo.println(out, "<data-source id=\""+SQLPowerUtils.escapeXML(id)+"\">");
                ioo.indent++;
                Iterator pit = ds.getPropertiesMap().entrySet().iterator();
                while (pit.hasNext()) {
                    Map.Entry ent = (Map.Entry) pit.next();
                    if (ent.getValue() != null) {
                        ioo.println(out, "<property key="+quote((String) ent.getKey())+" value="+quote((String) ent.getValue())+" />");
                    }
                }
                ioo.indent--;
                ioo.println(out, "</data-source>");
                dsNum++;
            }
            dsNum++;
        }
        ioo.indent--;
        ioo.println(out, "</project-data-sources>");
    }
    

    private void saveDDLGenerator(PrintWriter out) throws IOException {
        ioo.print(out, "<ddl-generator"
                +" type=\""+ session.getDDLGenerator().getClass().getName()+"\""
                +" allow-connection=\""+session.getDDLGenerator().getAllowConnection()+"\"");
        if (session.getDDLGenerator().getTargetCatalog() != null) {
            ioo.niprint(out, " target-catalog=\""+SQLPowerUtils.escapeXML(session.getDDLGenerator().getTargetCatalog())+"\"");
        }
        if (session.getDDLGenerator().getTargetSchema() != null) {
            ioo.niprint(out, " target-schema=\""+SQLPowerUtils.escapeXML(session.getDDLGenerator().getTargetSchema())+"\"");
        }
        ioo.niprint(out, ">");
        ioo.indent++;
        if (session.getDDLGenerator().getFile() != null) {
            ioo.println(out, "<file path=\""+SQLPowerUtils.escapeXML(session.getDDLGenerator().getFile().getPath())+"\" />");
        }
        ioo.indent--;
        ioo.println(out, "</ddl-generator>");
    }
    
    private void saveCreateKettleJobSettings(PrintWriter out) throws IOException {
        ioo.print(out, "<create-kettle-job-settings");
        ioo.niprint(out, " filePath=\"" + SQLPowerUtils.escapeXML(session.getCreateKettleJob().getFilePath()) + "\"");
        ioo.niprint(out, " jobName=\"" + SQLPowerUtils.escapeXML(session.getCreateKettleJob().getJobName()) + "\"");
        ioo.niprint(out, " schemaName=\"" + SQLPowerUtils.escapeXML(session.getCreateKettleJob().getSchemaName()) + "\"");
        ioo.niprint(out, " kettleJoinType=\"" + session.getCreateKettleJob().getKettleJoinType() + "\"");
        ioo.niprint(out, " savingToFile=\"" + session.getCreateKettleJob().isSavingToFile() + "\"");
        ioo.niprintln(out, " />");
    }

    /**
     * Writes out the CompareDM settings for this project unless the user has not
     * used that feature.
     *
     * @param out
     * @throws IOException
     */
    private void saveCompareDMSettings(PrintWriter out) throws IOException {

        if ( !session.getCompareDMSettings().getSaveFlag() )
            return;
        ioo.print(out, "<compare-dm-settings");
        ioo.print(out, " sqlScriptFormat=\""+SQLPowerUtils.escapeXML(session.getCompareDMSettings().getSqlScriptFormat())+"\"");
        ioo.print(out, " outputFormatAsString=\""+SQLPowerUtils.escapeXML(session.getCompareDMSettings().getOutputFormatAsString())+"\"");
        ioo.println(out, ">");
        ioo.indent++;
        ioo.print(out, "<source-stuff");
        saveSourceOrTargetAttributes(out, session.getCompareDMSettings().getSourceSettings());
        ioo.print(out, "/>");
        ioo.print(out, "<target-stuff");
        saveSourceOrTargetAttributes(out, session.getCompareDMSettings().getTargetSettings());
        ioo.print(out, "/>");
        ioo.indent--;
        ioo.println(out, "</compare-dm-settings>");
    }


    private void saveSourceOrTargetAttributes(PrintWriter out, SourceOrTargetSettings sourceSettings) {
        ioo.print(out, " datastoreTypeAsString=\""+SQLPowerUtils.escapeXML(sourceSettings.getDatastoreTypeAsString())+"\"");
        if (sourceSettings.getConnectName() != null)
            ioo.print(out, " connectName=\""+SQLPowerUtils.escapeXML(sourceSettings.getConnectName())+"\"");

        if (sourceSettings.getCatalog() != null)
            ioo.print(out, " catalog=\""+SQLPowerUtils.escapeXML(sourceSettings.getCatalog())+"\"");
        if (sourceSettings.getSchema() != null)
            ioo.print(out, " schema=\""+SQLPowerUtils.escapeXML(sourceSettings.getSchema())+"\"");
        ioo.print(out, " filePath=\""+SQLPowerUtils.escapeXML(sourceSettings.getFilePath())+"\"");

    }
    /**
     * Creates a &lt;source-databases&gt; element which contains zero
     * or more &lt;database&gt; elements.
     * @param out2
     */
    private void saveSourceDatabases(PrintWriter out) throws IOException, ArchitectException {
        ioo.println(out, "<source-databases>");
        ioo.indent++;
        SQLObject dbTreeRoot = (SQLObject) session.getSourceDatabases().getModel().getRoot();
        Iterator it = dbTreeRoot.getChildren().iterator();
        while (it.hasNext()) {
            SQLObject o = (SQLObject) it.next();
            if (o != session.getPlayPen().getDatabase()) {
                saveSQLObject(out, o);
            }
        }
        ioo.indent--;
        ioo.println(out, "</source-databases>");
    }

    /**
     * Recursively walks through the children of db, writing to the
     * output file all SQLRelationship objects encountered.
     */
    private void saveRelationships(PrintWriter out, SQLDatabase db) throws ArchitectException, IOException {
        ioo.println(out, "<relationships>");
        ioo.indent++;
        Iterator it = db.getChildren().iterator();
        while (it.hasNext()) {
            saveRelationshipsRecurse(out, (SQLObject) it.next());
        }
        ioo.indent--;
        ioo.println(out, "</relationships>");
    }

    /**
     * The recursive subroutine of saveRelationships.
     */
    private void saveRelationshipsRecurse(PrintWriter out, SQLObject o) throws ArchitectException, IOException {
        if ( (!session.isSavingEntireSource()) && (!o.isPopulated()) ) {
            return;
        } else if (o instanceof SQLRelationship) {
            saveSQLObject(out, o);
        } else if (o.allowsChildren()) {
            Iterator it = o.getChildren().iterator();
            while (it.hasNext()) {
                saveRelationshipsRecurse(out, (SQLObject) it.next());
            }
        }
    }

    private void saveTargetDatabase(PrintWriter out) throws IOException, ArchitectException {
        SQLDatabase db = (SQLDatabase) session.getPlayPen().getDatabase();
        ioo.println(out, "<target-database dbcs-ref="+
                quote(dbcsIdMap.get(db.getDataSource()).toString())+ ">");
        ioo.indent++;
        Iterator it = db.getChildren().iterator();
        while (it.hasNext()) {
            saveSQLObject(out, (SQLObject) it.next());
        }
        saveRelationships(out, db);
        ioo.indent--;
        ioo.println(out, "</target-database>");
    }

    private void savePlayPen(PrintWriter out) throws IOException, ArchitectException {
        ioo.println(out, "<play-pen>");
        ioo.indent++;
        for(int i = session.getPlayPen().getTablePanes().size()-1; i>= 0; i--) {
            TablePane tp = session.getPlayPen().getTablePanes().get(i);
            Point p = tp.getLocation();
            ioo.println(out, "<table-pane table-ref="+quote(objectIdMap.get(tp.getModel()).toString())+""
                    +" x=\""+p.x+"\" y=\""+p.y+"\" />");
            if (pm != null) {
                pm.setProgress(++progress);
            }
        }

        Iterator it = session.getPlayPen().getRelationships().iterator();
        while (it.hasNext()) {
            Relationship r = (Relationship) it.next();
            ioo.println(out, "<table-link relationship-ref="+quote(objectIdMap.get(r.getModel()).toString())
                    +" pk-x=\""+r.getPkConnectionPoint().x+"\""
                    +" pk-y=\""+r.getPkConnectionPoint().y+"\""
                    +" fk-x=\""+r.getFkConnectionPoint().x+"\""
                    +" fk-y=\""+r.getFkConnectionPoint().y+"\" />");
        }
        ioo.indent--;
        ioo.println(out, "</play-pen>");
    }

    /**
     * Save all of the profiling information.
     * @param out
     */
    private void saveProfiles(PrintWriter out) {
        TableProfileManager profmgr;
        if (session.getProfileManager() instanceof TableProfileManager) {
            profmgr = (TableProfileManager) session.getProfileManager();
        } else {
            throw new ArchitectRuntimeException(new ArchitectException("Session.getProfileManager should be a TableProfileManager"));
        }
        ioo.println(out, "<profiles topNCount=\""+profmgr.getDefaultProfileSettings().getTopNCount()+"\">");
        ioo.indent++;

        List<TableProfileResult> tableResults = profmgr.getResults();
        
        for (TableProfileResult tableResult : tableResults) {
            printCommonItems(out, tableResult);
            ioo.print(out, " rowCount=\"" + tableResult.getRowCount() + "\"");
            ioo.niprintln(out, "/>");
            
            List<ColumnProfileResult> columnProfileResults = tableResult.getColumnProfileResults();
            for (ColumnProfileResult cpr : columnProfileResults) {
                printCommonItems(out, cpr);
                ioo.niprint(out, " avgLength=\"" + cpr.getAvgLength() + "\"");

                ioo.niprint(out, " minLength=\"" + cpr.getMinLength() + "\"");
                ioo.niprint(out, " maxLength=\"" + cpr.getMaxLength() + "\"");
                ioo.niprint(out, " nullCount=\"" + cpr.getNullCount() + "\"");
                ioo.niprint(out, " distinctValueCount=\"" + cpr.getDistinctValueCount() + "\"");
                ioo.niprintln(out, ">");

                ioo.indent++;

                if ( cpr.getAvgValue() != null ) {
                    ioo.println(out, "<avgValue type=\"" +
                            cpr.getAvgValue().getClass().getName() +
                            "\" value=\""+
                            SQLPowerUtils.escapeXML(String.valueOf(cpr.getAvgValue())) +
                            "\"/>" );
                }
                if ( cpr.getMaxValue() != null ) {
                    ioo.println(out, "<maxValue type=\"" +
                            cpr.getMaxValue().getClass().getName() +
                            "\" value=\""+
                            SQLPowerUtils.escapeXML(String.valueOf(cpr.getMaxValue())) +
                            "\"/>" );
                }
                if ( cpr.getMinValue() != null ) {
                    ioo.println(out, "<minValue type=\"" +
                            cpr.getMinValue().getClass().getName() +
                            "\" value=\""+
                            SQLPowerUtils.escapeXML(String.valueOf(cpr.getMinValue())) +
                            "\"/>" );
                }

                List<ColumnValueCount> valueCount = cpr.getValueCount();
                if (valueCount != null) {
                    for (ColumnValueCount count : valueCount) {
                        ioo.println(out, "<topNvalue count=\""+
                                count.getCount()+
                                "\" type=\"" +
                                (count.getValue() == null ? "" : count.getValue().getClass().getName()) +
                                "\" value=\""+
                                SQLPowerUtils.escapeXML(String.valueOf(count.getValue()))+
                                "\"/>" );
                    }
                }
                ioo.indent--;

                ioo.println(out, "</profile-result>");
            }
        }
        ioo.println(out, "</profiles>");
        ioo.indent--;
    }

    private void printCommonItems(PrintWriter out, ProfileResult profileResult) {
        SQLObject profiledObject = profileResult.getProfiledObject();
        ioo.print(out, "<profile-result ref-id=\""+objectIdMap.get(profiledObject)+"\"" +
                " type=\"" + profileResult.getClass().getName() + "\"" +
                " createStartTime=\""+profileResult.getCreateStartTime()+"\"" +
                " createEndTime=\""+profileResult.getCreateEndTime()+"\"" +
                " exception=\""+(profileResult.getException() == null ? "false" : "true")+"\"");
        if (profileResult.getException() != null) {
            ioo.niprint(out, " exception-type=\""+SQLPowerUtils.escapeXML(profileResult.getException().getClass().getName())+"\"");
            ioo.niprint(out, " exception-message=\""+SQLPowerUtils.escapeXML(profileResult.getException().getMessage())+"\"");
        }
    }
    
    /**
     * Creates an XML element describing the given SQLObject and
     * writes it to the <code>out</code> PrintWriter.
     *
     * <p>Design notes: Attribute names that are straight property
     * contents (such as name or defaultValue) are chosen so that
     * automatic JavaBeans population of object properties is
     * possible.  For the same reasons, attributes that need
     * non-automatic population (such as reference properties like
     * pkColumn) are named to purposely disable automatic JavaBeans
     * property setting.  In the pkColumn example, the XML attribute
     * name would be pk-column-ref.  Special code in the load routine
     * is responsible for deferencing the attribute and setting the
     * property manually.
     */
    private void saveSQLObject(PrintWriter out, SQLObject o) throws IOException, ArchitectException {
        String id = (String) objectIdMap.get(o);
        if (id != null) {
            ioo.println(out, "<reference ref-id=\""+SQLPowerUtils.escapeXML(id)+"\" />");
            return;
        }

        String type;
        Map<String,Object> propNames = new TreeMap<String,Object>();

        // properties of all SQLObject types
        propNames.put("physicalName", o.getPhysicalName());
        propNames.put("name", o.getName()); // note: there was no name attrib for SQLDatabase, SQLRelationship.ColumnMapping, and SQLExceptionNode

        if (o instanceof SQLDatabase) {
            id = "DB"+objectIdMap.size();
            type = "database";
            propNames.put("dbcs-ref", dbcsIdMap.get(((SQLDatabase) o).getDataSource()));
        } else if (o instanceof SQLCatalog) {
            id = "CAT"+objectIdMap.size();
            type = "catalog";
            propNames.put("nativeTerm", ((SQLCatalog) o).getNativeTerm());
        } else if (o instanceof SQLSchema) {
            id = "SCH"+objectIdMap.size();
            type = "schema";
            propNames.put("nativeTerm", ((SQLSchema) o).getNativeTerm());
        } else if (o instanceof SQLTable) {
            id = "TAB"+objectIdMap.size();
            type = "table";
            propNames.put("remarks", ((SQLTable) o).getRemarks());
            propNames.put("objectType", ((SQLTable) o).getObjectType());
            // don't save primary key name. It is a propery of the PK index, not the table.
            if (pm != null) {
                pm.setProgress(++progress);
            }
        } else if (o instanceof SQLTable.Folder) {
            id = "FOL"+objectIdMap.size();
            type = "folder";
            propNames.put("type", new Integer(((SQLTable.Folder) o).getType()));
        } else if (o instanceof SQLColumn) {
            id = "COL"+objectIdMap.size();
            type = "column";
            SQLColumn sourceCol = ((SQLColumn) o).getSourceColumn();
            if (sourceCol != null) {
                propNames.put("source-column-ref", objectIdMap.get(sourceCol));
            }
            propNames.put("type", new Integer(((SQLColumn) o).getType()));
            propNames.put("sourceDataTypeName", ((SQLColumn) o).getSourceDataTypeName());
            propNames.put("scale", new Integer(((SQLColumn) o).getScale()));
            propNames.put("precision", new Integer(((SQLColumn) o).getPrecision()));
            propNames.put("nullable", new Integer(((SQLColumn) o).getNullable()));
            propNames.put("remarks", ((SQLColumn) o).getRemarks());
            propNames.put("defaultValue", ((SQLColumn) o).getDefaultValue());
            propNames.put("primaryKeySeq", ((SQLColumn) o).getPrimaryKeySeq());
            propNames.put("autoIncrement", new Boolean(((SQLColumn) o).isAutoIncrement()));
            propNames.put("referenceCount", new Integer(((SQLColumn)o).getReferenceCount()));
        } else if (o instanceof SQLRelationship) {
            id = "REL"+objectIdMap.size();
            type = "relationship";
            propNames.put("pk-table-ref", objectIdMap.get(((SQLRelationship) o).getPkTable()));
            propNames.put("fk-table-ref", objectIdMap.get(((SQLRelationship) o).getFkTable()));
            propNames.put("updateRule", new Integer(((SQLRelationship) o).getUpdateRule()));
            propNames.put("deleteRule", new Integer(((SQLRelationship) o).getDeleteRule()));
            propNames.put("deferrability", new Integer(((SQLRelationship) o).getDeferrability()));
            propNames.put("pkCardinality", new Integer(((SQLRelationship) o).getPkCardinality()));
            propNames.put("fkCardinality", new Integer(((SQLRelationship) o).getFkCardinality()));
            propNames.put("identifying", new Boolean(((SQLRelationship) o).isIdentifying()));
        } else if (o instanceof SQLRelationship.ColumnMapping) {
            id = "CMP"+objectIdMap.size();
            type = "column-mapping";
            propNames.put("pk-column-ref", objectIdMap.get(((SQLRelationship.ColumnMapping) o).getPkColumn()));
            propNames.put("fk-column-ref", objectIdMap.get(((SQLRelationship.ColumnMapping) o).getFkColumn()));
        } else if (o instanceof SQLExceptionNode) {
            id = "EXC"+objectIdMap.size();
            type = "sql-exception";
            propNames.put("message", ((SQLExceptionNode) o).getMessage());
        } else if (o instanceof SQLIndex) {
            id = "IDX"+objectIdMap.size();
            type = "index";
            SQLIndex index = (SQLIndex) o;
            propNames.put("unique", index.isUnique());
            propNames.put("qualifier", index.getQualifier());
            propNames.put("index-type", index.getType().name());
            propNames.put("primaryKeyIndex", index.isPrimaryKeyIndex());
            propNames.put("filterCondition", index.getFilterCondition());
        } else if (o instanceof SQLIndex.Column) {
            id = "IDC"+objectIdMap.size();
            type = "index-column";
            SQLIndex.Column col = (SQLIndex.Column) o;
            if (col.getColumn() != null) {
                propNames.put("column-ref", objectIdMap.get(col.getColumn()));
            }
            propNames.put("ascending", col.isAscending());
            propNames.put("descending", col.isDescending());
        } else {
            throw new UnsupportedOperationException("Whoops, the SQLObject type "
                    +o.getClass().getName()+" is not supported!");
        }

        objectIdMap.put(o, id);

        boolean skipChildren = false;

        //ioo.print("<"+type+" hashCode=\""+o.hashCode()+"\" id=\""+id+"\" ");  // use this for debugging duplicate object problems
        ioo.print(out, "<"+type+" id="+quote(id)+" ");

        if (o.allowsChildren() && o.isPopulated() && o.getChildCount() == 1 && o.getChild(0) instanceof SQLExceptionNode) {
            // if the only child is an exception node, just save the parent as non-populated
            ioo.niprint(out, "populated=\"false\" ");
            skipChildren = true;
        } else if ( (!session.isSavingEntireSource()) && (!o.isPopulated()) ) {
            ioo.niprint(out, "populated=\"false\" ");
        } else {
            ioo.niprint(out, "populated=\"true\" ");
        }

        Iterator props = propNames.keySet().iterator();
        while (props.hasNext()) {
            Object key = props.next();
            Object value = propNames.get(key);
            if (value != null) {
                ioo.niprint(out, key+"="+quote(value.toString())+" ");
            }
        }
        if ( (!skipChildren) && o.allowsChildren() && (session.isSavingEntireSource() || o.isPopulated()) ) {
            ioo.niprintln(out, ">");
            Iterator children = o.getChildren().iterator();
            ioo.indent++;
            while (children.hasNext()) {
                SQLObject child = (SQLObject) children.next();
                if ( ! (child instanceof SQLRelationship)) {
                    saveSQLObject(out, child);
                }
            }
            if (o instanceof SQLDatabase) {
                saveRelationships(out, (SQLDatabase) o);
            }
            ioo.indent--;
            ioo.println(out, "</"+type+">");
        } else {
            ioo.niprintln(out, "/>");
        }
    }

    private String quote(String str) {
        return "\""+SQLPowerUtils.escapeXML(str)+"\"";
    }
    // ------------------- accessors and mutators ---------------------

  

    /**
     * Gets the value of file
     *
     * @return the value of file
     */
    public File getFile()  {
        return this.file;
    }

    /**
     * Sets the value of file
     *
     * @param argFile Value to assign to this.file
     */
    public void setFile(File argFile) {
        this.file = argFile;
    }

   
    /**
     * Adds all the tables in the given database into the playpen database.  This is really only
     * for loading projects, so please think twice about using it for other stuff.
     *
     * @param db The database to add tables from.  The database must contain tables directly.
     * @throws ArchitectException If adding the tables of db fails
     */
    public void addAllTablesFrom(SQLDatabase db) throws ArchitectException {
        SQLDatabase ppdb = session.getPlayPen().getDatabase();
        for (SQLObject table : (List<SQLObject>) db.getChildren()) {
            ppdb.addChild(table);
        }
    }

    /**
     * See {@link #modified}.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * See {@link #modified}.
     */
    public void setModified(boolean modified) {
        if (logger.isDebugEnabled()) logger.debug("Project modified: "+modified);
        this.modified = modified;
    }
}