package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.ToolTipManager;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.IOUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLExceptionNode;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.CompareDMSettings.SourceOrTargetSettings;
import ca.sqlpower.architect.swingui.event.PlayPenComponentEvent;
import ca.sqlpower.architect.swingui.event.PlayPenComponentListener;
import ca.sqlpower.architect.undo.UndoManager;

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
    private String name;
    private DBTree sourceDatabases;
    private PlayPen playPen;
    private UndoManager undoManager;
    private File file;
    private GenericDDLGenerator ddlGenerator;
    private boolean savingEntireSource;
    private PLExport plExport;
    private CompareDMSettings compareDMSettings;
    private ProfileManager profileManager;
    final JDialog profileDialog = new JDialog(ArchitectFrame.getMainInstance(), "Table Profiles");
    final ArrayList<SQLObject> filter = new ArrayList<SQLObject>();

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

    /**
     * Sets up a new project with the given name.
     * @throws
     */
    public SwingUIProject(String name) throws ArchitectException {
        this.name = name;

        SQLDatabase ppdb = new SQLDatabase();

        PlayPen pp = new PlayPen(ppdb);
        ToolTipManager.sharedInstance().registerComponent(pp);
        setPlayPen(pp);
        List initialDBList = new ArrayList();
        initialDBList.add(playPen.getDatabase());
        profileManager = new ProfileManager();
        this.sourceDatabases = new DBTree(initialDBList,profileManager);
        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new ArchitectException("SQL Error in ddlGenerator",e);
        }
        plExport = new PLExport();
        compareDMSettings = new CompareDMSettings();
        undoManager = new UndoManager(pp);
    }

    // ------------- READING THE PROJECT FILE ---------------

    // FIXME: this should static and return a new instance of SwingUIProject
    public void load(InputStream in) throws IOException, ArchitectException {
        dbcsIdMap = new HashMap();
        objectIdMap = new HashMap();

        // use digester to read from file
        try {
            setupDigester().parse(in);
        } catch (SAXException ex) {
            logger.error("SAX Exception in config file parse!", ex);
            throw new ArchitectException("Syntax error in Project file", ex);
        } catch (IOException ex) {
            logger.error("IO Exception in config file parse!", ex);
            throw new ArchitectException("I/O Error", ex);
        } catch (Exception ex) {
            logger.error("General Exception in config file parse!", ex);
            throw new ArchitectException("Unexpected Exception", ex);
        }

        ((SQLObject) sourceDatabases.getModel().getRoot()).addChild(0, playPen.getDatabase());
        setModified(false);
        // TODO change this to load the undo history from a file
        undoManager.discardAllEdits();
    }



    private Digester setupDigester() {
        Digester d = new Digester();
        d.setValidating(false);
        d.push(this);

        // project name
        d.addCallMethod("architect-project/project-name", "setName", 0); // argument is element body text

        // source DB connection specs (deprecated in favour of project-data-sources)
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
        //d.addSetNext("architect-project/project-data-sources/data-source", );

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
        d.addSetNext("*/profiles/profile-result", "putResult");

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
     * Creates a ArchitectDataSource object and puts a mapping from its
     * id (in the attributes) to the new instance into the dbcsIdMap.
     */
    private class DBCSFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            ArchitectDataSource dbcs = new ArchitectDataSource();
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
            SQLDatabase ppdb = playPen.getDatabase();

            String dbcsid = attributes.getValue("dbcs-ref");
            if (dbcsid != null) {
                ppdb.setDataSource((ArchitectDataSource) dbcsIdMap.get(dbcsid));
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
                db.setDataSource((ArchitectDataSource) dbcsIdMap.get(dbcsid));
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
            if (id != null) {
                objectIdMap.put(id, tab);
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

    private class TablePaneFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            int x = Integer.parseInt(attributes.getValue("x"));
            int y = Integer.parseInt(attributes.getValue("y"));
            SQLTable tab = (SQLTable) objectIdMap.get(attributes.getValue("table-ref"));
            TablePane tp = new TablePane(tab, playPen);
            playPen.addTablePane(tp, new Point(x, y));
            return tp;
        }
    }

    private class PPRelationshipFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Relationship r = null;
            try {
                SQLRelationship rel =
                    (SQLRelationship) objectIdMap.get(attributes.getValue("relationship-ref"));
                r = new Relationship(playPen, rel);
                playPen.addRelationship(r);

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

            return getCompareDMSettings();
        }
    }

    private class CompareDMStuffSettingFactory extends AbstractObjectCreationFactory {
        private boolean source;
        public CompareDMStuffSettingFactory(boolean source) {
            this.source = source;
        }
        public Object createObject(Attributes attributes) {
            if ( source )
                return getCompareDMSettings().getSourceSettings();
            else
                return getCompareDMSettings().getTargetSettings();
        }
    }

    /**
     * Just returns the existing profile manager (this way, all the profile results
     * will get added to the existing one)
     */
    private class ProfileManagerFactory extends AbstractObjectCreationFactory {
        @Override
        public Object createObject(Attributes attributes) throws ArchitectException {
            return profileManager;
        }
    }

    private class ProfileResultFactory extends AbstractObjectCreationFactory {
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
                return new TableProfileResult(t);
            } else if (className.equals(ColumnProfileResult.class.getName())) {
                SQLColumn c = (SQLColumn) objectIdMap.get(refid);
                return new ColumnProfileResult(c);
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
            if (savingEntireSource) {
                pmMax = ArchitectUtils.countTablesSnapshot((SQLObject) sourceDatabases.getModel().getRoot());
            } else {
                pmMax = ArchitectUtils.countTables((SQLObject) sourceDatabases.getModel().getRoot());
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

    IOUtils ioo = new IOUtils();
    /**
     * Do just the writing part of save, given a PrintWriter
     * @param out - the file to write to
     * @return True iff the save completed OK
     * @throws IOException
     * @throws ArchitectException
     */
    public void save(PrintWriter out, String encoding) throws IOException, ArchitectException {
        objectIdMap = new HashMap();
        dbcsIdMap = new HashMap();
        ioo.indent = 0;

        try {
            ioo.println(out, "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>");
            ioo.println(out, "<architect-project version=\"1.0\" appversion=\""+ArchitectUtils.APP_VERSION+"\">");
            ioo.indent++;
            ioo.println(out, "<project-name>"+ArchitectUtils.escapeXML(name)+"</project-name>");
            saveDataSources(out);
            saveSourceDatabases(out);
            saveTargetDatabase(out);
            saveDDLGenerator(out);
            saveCompareDMSettings(out);
            savePlayPen(out);
            saveProfiles(out);
            ioo.indent--;
            ioo.println(out, "</architect-project>");
            setModified(false);
        } finally {
            if (out != null) out.close();
        }
    }

    private void saveDataSources(PrintWriter out) throws IOException, ArchitectException {
        ioo.println(out, "<project-data-sources>");
        ioo.indent++;
        int dsNum = 0;
        SQLObject dbTreeRoot = (SQLObject) sourceDatabases.getModel().getRoot();
        Iterator it = dbTreeRoot.getChildren().iterator();
        while (it.hasNext()) {
            SQLObject o = (SQLObject) it.next();
            ArchitectDataSource ds = ((SQLDatabase) o).getDataSource();
            if (ds != null) {
                String id = (String) dbcsIdMap.get(ds);
                if (id == null) {
                    id = "DS"+dsNum;
                    dbcsIdMap.put(ds, id);
                }
                ioo.println(out, "<data-source id=\""+ArchitectUtils.escapeXML(id)+"\">");
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
                +" type=\""+ddlGenerator.getClass().getName()+"\""
                +" allow-connection=\""+ddlGenerator.getAllowConnection()+"\"");
        if (ddlGenerator.getTargetCatalog() != null) {
            ioo.niprint(out, " target-catalog=\""+ArchitectUtils.escapeXML(ddlGenerator.getTargetCatalog())+"\"");
        }
        if (ddlGenerator.getTargetSchema() != null) {
            ioo.niprint(out, " target-schema=\""+ArchitectUtils.escapeXML(ddlGenerator.getTargetSchema())+"\"");
        }
        ioo.niprint(out, ">");
        ioo.indent++;
        if (ddlGenerator.getFile() != null) {
            ioo.println(out, "<file path=\""+ArchitectUtils.escapeXML(ddlGenerator.getFile().getPath())+"\" />");
        }
        ioo.indent--;
        ioo.println(out, "</ddl-generator>");
    }


    private void saveCompareDMSettings(PrintWriter out) throws IOException {

        //If the user never uses compareDM function, the saving process
        //would fail since some of the return values of saving compareDM
        //settings would be null.  Therefore the saveFlag is used as an
        //indicator to tell if the user went into compareDM or not.
        if ( !compareDMSettings.getSaveFlag() )
            return;
        ioo.print(out, "<compare-dm-settings");
        ioo.print(out, " sqlScriptFormat=\""+ArchitectUtils.escapeXML(compareDMSettings.getSqlScriptFormat())+"\"");
        ioo.print(out, " outputFormatAsString=\""+ArchitectUtils.escapeXML(compareDMSettings.getOutputFormatAsString())+"\"");
        ioo.println(out, ">");
        ioo.indent++;
        ioo.print(out, "<source-stuff");
        saveSourceOrTargetAttributes(out, compareDMSettings.getSourceSettings());
        ioo.print(out, "/>");
        ioo.print(out, "<target-stuff");
        saveSourceOrTargetAttributes(out, compareDMSettings.getTargetSettings());
        ioo.print(out, "/>");
        ioo.indent--;
        ioo.println(out, "</compare-dm-settings>");
    }


    private void saveSourceOrTargetAttributes(PrintWriter out, SourceOrTargetSettings sourceSettings) {
        ioo.print(out, " datastoreTypeAsString=\""+ArchitectUtils.escapeXML(sourceSettings.getDatastoreTypeAsString())+"\"");
        if (sourceSettings.getConnectName() != null)
            ioo.print(out, " connectName=\""+ArchitectUtils.escapeXML(sourceSettings.getConnectName())+"\"");

        if (sourceSettings.getCatalog() != null)
            ioo.print(out, " catalog=\""+ArchitectUtils.escapeXML(sourceSettings.getCatalog())+"\"");
        if (sourceSettings.getSchema() != null)
            ioo.print(out, " schema=\""+ArchitectUtils.escapeXML(sourceSettings.getSchema())+"\"");
        ioo.print(out, " filePath=\""+ArchitectUtils.escapeXML(sourceSettings.getFilePath())+"\"");

    }
    /**
     * Creates a &lt;source-databases&gt; element which contains zero
     * or more &lt;database&gt; elements.
     * @param out2
     */
    private void saveSourceDatabases(PrintWriter out) throws IOException, ArchitectException {
        ioo.println(out, "<source-databases>");
        ioo.indent++;
        SQLObject dbTreeRoot = (SQLObject) sourceDatabases.getModel().getRoot();
        Iterator it = dbTreeRoot.getChildren().iterator();
        while (it.hasNext()) {
            SQLObject o = (SQLObject) it.next();
            if (o != playPen.getDatabase()) {
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
        if ( (!savingEntireSource) && (!o.isPopulated()) ) {
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
        SQLDatabase db = (SQLDatabase) playPen.getDatabase();
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
        Iterator it = playPen.getTablePanes().iterator();
        while (it.hasNext()) {
            TablePane tp = (TablePane) it.next();
            Point p = tp.getLocation();
            ioo.println(out, "<table-pane table-ref="+quote(objectIdMap.get(tp.getModel()).toString())+""
                    +" x=\""+p.x+"\" y=\""+p.y+"\" />");
            if (pm != null) {
                pm.setProgress(++progress);
                pm.setNote(tp.getModel().getShortDisplayName());
            }
        }

        it = playPen.getRelationships().iterator();
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
        ProfileManager profmgr = getProfileManager();
        ioo.println(out, "<profiles topNCount=\""+profmgr.getTopNCount()+"\">");
        ioo.indent++;
        Map<SQLObject, ProfileResult> results = profmgr.getResults();
        for (Map.Entry<SQLObject, ProfileResult> e : results.entrySet()) {
            SQLObject so = e.getKey();
            ProfileResult profileResult = e.getValue();
            ioo.print(out, "<profile-result ref-id=\""+objectIdMap.get(so)+"\"" +
                            " type=\"" + profileResult.getClass().getName() + "\"" +
                            " createStartTime=\""+profileResult.getCreateStartTime()+"\"" +
                            " createEndTime=\""+profileResult.getCreateEndTime()+"\"" +
                            " error=\""+profileResult.isError()+"\"");
            if (profileResult.getException() != null) {
                ioo.niprint(out, " exception-type=\""+ArchitectUtils.escapeXML(profileResult.getException().getClass().getName())+"\"");
                ioo.niprint(out, " exception-message=\""+ArchitectUtils.escapeXML(profileResult.getException().getMessage())+"\"");
            }
            if (profileResult instanceof TableProfileResult) {
                TableProfileResult tpr = (TableProfileResult) profileResult;
                ioo.print(out, " rowCount=\""+tpr.getRowCount()+"\"");
                ioo.niprintln(out, "/>");
            } else if (profileResult instanceof ColumnProfileResult) {
                ColumnProfileResult cpr = (ColumnProfileResult) profileResult;
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
                            ArchitectUtils.escapeXML(String.valueOf(cpr.getAvgValue())) +
                            "\"/>" );
                }
                if ( cpr.getMaxValue() != null ) {
                    ioo.println(out, "<maxValue type=\"" +
                            cpr.getMaxValue().getClass().getName() +
                            "\" value=\""+
                            ArchitectUtils.escapeXML(String.valueOf(cpr.getMaxValue())) +
                            "\"/>" );
                }
                if ( cpr.getMinValue() != null ) {
                    ioo.println(out, "<minValue type=\"" +
                            cpr.getMinValue().getClass().getName() +
                            "\" value=\""+
                            ArchitectUtils.escapeXML(String.valueOf(cpr.getMinValue())) +
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
                                ArchitectUtils.escapeXML(String.valueOf(count.getValue()))+
                                "\"/>" );
                    }
                }
                ioo.indent--;

                ioo.println(out, "</profile-result>");
            } else {
                String message = "Unknown ProfileResult Subclass: " + profileResult.getClass().getName();
                ioo.niprintln(out, "/> <!-- " + message + "-->");
                logger.error(message);
            }
        }
        ioo.println(out, "</profiles>");
        ioo.indent--;
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
            ioo.println(out, "<reference ref-id=\""+ArchitectUtils.escapeXML(id)+"\" />");
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
            propNames.put("primaryKeyName", ((SQLTable) o).getPrimaryKeyName());
            propNames.put("physicalPrimaryKeyName", ((SQLTable) o).getPhysicalPrimaryKeyName());
            if (pm != null) {
                pm.setProgress(++progress);
                pm.setNote(o.getShortDisplayName());
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
        } else if ( (!savingEntireSource) && (!o.isPopulated()) ) {
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
        if ( (!skipChildren) && o.allowsChildren() && (savingEntireSource || o.isPopulated()) ) {
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
        return "\""+ArchitectUtils.escapeXML(str)+"\"";
    }
    // ------------------- accessors and mutators ---------------------

    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName()  {
        return this.name;
    }

    /**
     * Sets the value of name
     *
     * @param argName Value to assign to this.name
     */
    public void setName(String argName) {
        this.name = argName;
    }

    /**
     * Gets the value of sourceDatabases
     *
     * @return the value of sourceDatabases
     */
    public DBTree getSourceDatabases()  {
        return this.sourceDatabases;
    }

    /**
     * Sets the value of sourceDatabases
     *
     * @param argSourceDatabases Value to assign to this.sourceDatabases
     */
    public void setSourceDatabases(DBTree argSourceDatabases) {
        this.sourceDatabases = argSourceDatabases;
    }

    public void setSourceDatabaseList(List databases) throws ArchitectException {
        this.sourceDatabases.setModel(new DBTreeModel(databases));
    }

    /**
     * Gets the target database in the playPen.
     */
    public SQLDatabase getTargetDatabase()  {
        return playPen.getDatabase();
    }

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
     * Gets the value of playPen
     *
     * @return the value of playPen
     */
    public PlayPen getPlayPen()  {
        return this.playPen;
    }

    /**
     * Adds all the tables in the given database into the playpen database.  This is really only
     * for loading projects, so please think twice about using it for other stuff.
     *
     * @param db The database to add tables from.  The database must contain tables directly.
     * @throws ArchitectException If adding the tables of db fails
     */
    public void addAllTablesFrom(SQLDatabase db) throws ArchitectException {
        SQLDatabase ppdb = playPen.getDatabase();
        for (SQLObject table : (List<SQLObject>) db.getChildren()) {
            ppdb.addChild(table);
        }
    }

    /**
     * Sets the value of playPen
     *
     * @param argPlayPen Value to assign to this.playPen
     */
    public void setPlayPen(PlayPen argPlayPen) {
        this.playPen = argPlayPen;
        UserSettings sprefs = ArchitectFrame.getMainInstance().getSprefs();
        if (sprefs != null) {
            playPen.setRenderingAntialiased(sprefs.getBoolean(SwingUserSettings.PLAYPEN_RENDER_ANTIALIASED, false));
        }
        new ProjectModificationWatcher(playPen);
    }

    public GenericDDLGenerator getDDLGenerator() {
        return ddlGenerator;
    }

    public void setDDLGenerator(GenericDDLGenerator generator) {
        ddlGenerator = generator;
    }

    public CompareDMSettings getCompareDMSettings() {
        return compareDMSettings;
    }
    public void setCompareDMSettings(CompareDMSettings compareDMSettings) {
        this.compareDMSettings = compareDMSettings;
    }

    /**
     * See {@link #savingEntireSource}.
     *
     * @return the value of savingEntireSource
     */
    public boolean isSavingEntireSource()  {
        return this.savingEntireSource;
    }

    /**
     * See {@link #savingEntireSource}.
     *
     * @param argSavingEntireSource Value to assign to this.savingEntireSource
     */
    public void setSavingEntireSource(boolean argSavingEntireSource) {
        this.savingEntireSource = argSavingEntireSource;
    }

    public PLExport getPLExport() {
        return plExport;
    }

    public void setPLExport(PLExport v) {
        plExport = v;
    }



    /**
     * The ProjectModificationWatcher watches a PlayPen's components and
     * business model for changes.  When it detects any, it marks the
     * project dirty.
     *
     * <p>Note: when we implement proper undo/redo support, this class should
     * be replaced with a hook into that system.
     */
    private class ProjectModificationWatcher implements SQLObjectListener, PlayPenComponentListener {

        /**
         * Sets up a new modification watcher on the given playpen.
         */
        public ProjectModificationWatcher(PlayPen pp) {
            try {
                ArchitectUtils.listenToHierarchy(this, pp.getDatabase());
            } catch (ArchitectException e) {
                logger.error("Can't listen to business model for changes", e);
            }
            PlayPenContentPane ppcp = pp.contentPane;
            ppcp.addPlayPenComponentListener(this);
        }

        /** Marks project dirty, and starts listening to new kids. */
        public void dbChildrenInserted(SQLObjectEvent e) {
            setModified(true);
            SQLObject[] newKids = e.getChildren();
            for (int i = 0; i < newKids.length; i++) {
                try {
                    ArchitectUtils.listenToHierarchy(this, newKids[i]);
                } catch (ArchitectException e1) {
                    logger.error("Couldn't listen to SQLObject hierarchy rooted at "+newKids[i], e1);
                }
            }
        }

        /** Marks project dirty, and stops listening to removed kids. */
        public void dbChildrenRemoved(SQLObjectEvent e) {
            setModified(true);
            SQLObject[] oldKids = e.getChildren();
            for (int i = 0; i < oldKids.length; i++) {
                oldKids[i].removeSQLObjectListener(this);
            }
        }

        /** Marks project dirty. */
        public void dbObjectChanged(SQLObjectEvent e) {
            setModified(true);
        }

        /** Marks project dirty and listens to new hierarchy. */
        public void dbStructureChanged(SQLObjectEvent e) {
            try {
                ArchitectUtils.listenToHierarchy(this, e.getSQLSource());
            } catch (ArchitectException e1) {
                logger.error("dbStructureChanged listener: Failed to listen to new project hierarchy", e1);
            }
        }

        public void componentMoved(PlayPenComponentEvent e) {

        }

        public void componentResized(PlayPenComponentEvent e) {
            setModified(true);
        }

        public void componentMoveStart(PlayPenComponentEvent e) {
            setModified(true);
        }

        public void componentMoveEnd(PlayPenComponentEvent e) {
            setModified(true);
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

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }
    public JDialog getProfileDialog() {
        return profileDialog;
    }
    public ArrayList<SQLObject> getFilter() {
        return filter;
    }

}
