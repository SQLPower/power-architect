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

package ca.sqlpower.architect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.SetPropertiesRule;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.SQLRelationship.Deferrability;
import ca.sqlpower.architect.SQLTable.Folder;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.xml.UnescapingSaxParser;

public class CoreProject {

    /*
     * Any Jakarta Commons BeanUtils converters needed by the Digester should
     * be registered here.  This guarantees they will be registered before
     * they're needed, and that they won't be registered more than once.
     */
    static {
        ConvertUtils.register(new DeferrabilityConverter(), Deferrability.class);
    }
    
//  ---------------- persistent properties -------------------

    protected File file;
    
    // ------------------ load and save support -------------------

    private static final Logger logger = Logger.getLogger(CoreProject.class);
    
    /**
     * Tracks whether or not this project has been modified since last saved.
     */
    protected boolean modified;

    /**
     * Don't let application exit while saving.
     */
    protected boolean saveInProgress;

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
    protected PrintWriter out;

    /**
     * During a LOAD, this map maps String ID codes to SQLObject instances.
     * During a SAVE, it holds mappings from SQLObject instance to String
     * ID (the inverse of the LOAD mapping).
     */
    protected Map objectIdMap;

    /**
     * During a LOAD, this map maps String ID codes to DBCS instances.
     * During a SAVE, it holds mappings from DBCS instance to String
     * ID (the inverse of the LOAD mapping).
     */
    protected Map dbcsIdMap;
    
    /**
     * The last value we sent to the progress monitor.
     */
    protected int progress = 0;
    
    protected ArchitectSession session;
    
    public CoreProject(ArchitectSession session) {
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

        SQLObject dbConnectionContainer = ((SQLObject) getSession().getRootObject());
        dbConnectionContainer.addChild(0, getSession().getTargetDatabase());
        getSession().getTargetDatabase().setPlayPenDatabase(true);

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
        for (SQLTable table : (List<SQLTable>)getSession().getTargetDatabase().getTables()) {

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
    }
    
    protected Digester setupDigester() throws ParserConfigurationException, SAXException {
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
     
        DDLGeneratorFactory ddlgFactory = new DDLGeneratorFactory();
        d.addFactoryCreate("architect-project/ddl-generator", ddlgFactory);
        d.addSetProperties("architect-project/ddl-generator");
        d.addSetNext("architect-project/ddl-generator", "setDDLGenerator");
        
        ProfileManagerFactory profileManagerFactory = new ProfileManagerFactory();
        d.addFactoryCreate("*/profiles", profileManagerFactory);
        d.addSetProperties("*/profiles");

        ProfileResultFactory profileResultFactory = new ProfileResultFactory();
        d.addFactoryCreate("*/profiles/profile-result", profileResultFactory);
        /* 
         * backward compatibility: the exception property used to be a boolean, and now it's an actual exception.
         * this causes an IllegalArgumentException when parsing old files.
         * this workaround tells the digester not to auto-map the exception property.
         */ 
        d.addRule("*/profiles/profile-result", new SetPropertiesRule(new String[] {"exception"}, new String[] {}));
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
        
        return d;
    }
    
    /**
     * Creates a SPDataSource object and puts a mapping from its
     * id (in the attributes) to the new instance into the dbcsIdMap.
     */
    private class DBCSFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            SPDataSource dbcs = new SPDataSource(getSession().getContext().getPlDotIni());
            
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
            SQLDatabase ppdb = getSession().getTargetDatabase();

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
            index.setType(attributes.getValue("index-type"));
    
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
            for (int i = 0; i < attributes.getLength(); i++) {
                logger.debug("Attribute: \"" + attributes.getQName(i) + "\" Value:"+attributes.getValue(i));
            }
            
            if (attributes.getValue("ascendingOrDescending") != null) {
                col.setAscendingOrDescending(SQLIndex.AscendDescend.valueOf(attributes.getValue("ascendingOrDescending")));
            }

            return col;
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

    private class FileFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            return new File(attributes.getValue("path"));
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
                tableProfileResult = new TableProfileResult(t, session.getProfileManager().getDefaultProfileSettings());
                
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
            
            String per = attributes.getValue("percent");
            double percent = -1;
            if (per != null) {
                percent = Double.valueOf(per);
            }
            
            String value = attributes.getValue("value");

            if (className == null || className.length() == 0 ) {
                return new ColumnValueCount(null,count, percent);
            } else if (className.equals(BigDecimal.class.getName()) ) {
                return new ColumnValueCount(new BigDecimal(value),count, percent);
            } else if (className.equals(Timestamp.class.getName()) ) {
                return new ColumnValueCount(new Timestamp( Timestamp.valueOf(value).getTime() ),count, percent);
            } else if (className.equals(String.class.getName()) ) {
                return new ColumnValueCount(new String(value),count, percent);
            } else {
                return new ColumnValueCount(new String(value),count, percent);
            }
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
    
    protected ArchitectSession getSession() {
        return session;
    }
    
    /**
     * Returns the file that this project was most recently
     * saved to or loaded from.
     */
    public File getFile()  {
        return this.file;
    }

    /**
     * Tells this project which file it was most recently
     * saved to or loaded from.
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
        SQLDatabase ppdb = getSession().getTargetDatabase();
        for (SQLObject table : (List<SQLObject>) db.getChildren()) {
            ppdb.addChild(table);
        }
    }
}
