package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.undo.UndoManager;

/** Used to load and store Projects.
 * XXX Consider rewriting using JAXB instead of Digester - after tests are in place.
 */
public class SwingUIProject {
	private static final Logger logger = Logger.getLogger(SwingUIProject.class);

	//  ---------------- persistent properties -------------------
	protected String name;
	protected DBTree sourceDatabases;
	protected PlayPen playPen;
	protected UndoManager undoManager;
	protected File file;
	protected GenericDDLGenerator ddlGenerator;
	protected boolean savingEntireSource;
	protected PLExport plExport;

	// ------------------ load and save support -------------------

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
	 * Used for saving only: this is the current indentation amount in
	 * the XML output file.
	 */
	protected int indent = 0;

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
	 * Shows progress during saves and loads.
	 */
	protected ProgressMonitor pm;

	/**
	 * The last value we sent to the progress monitor.
	 */
	protected int progress = 0;

	/**
	 * Sets up a new project with the given name.
	 */
	public SwingUIProject(String name) throws ArchitectException {
		this.name = name;
		PlayPen pp = new PlayPen(new SQLDatabase());
		ToolTipManager.sharedInstance().registerComponent(pp);
		setPlayPen(pp);
		List initialDBList = new ArrayList();
		initialDBList.add(playPen.getDatabase());
		this.sourceDatabases = new DBTree(initialDBList);
		ddlGenerator = new GenericDDLGenerator();
		plExport = new PLExport();
	}

	// ------------- READING THE PROJECT FILE ---------------
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
	}

	protected Digester setupDigester() {
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

		// target database hierarchy
		d.addFactoryCreate("architect-project/target-database", dbFactory);
		d.addSetProperties("architect-project/target-database");
		d.addSetNext("architect-project/target-database", "setTargetDatabase");

		// the play pen
		TablePaneFactory tablePaneFactory = new TablePaneFactory();
		d.addFactoryCreate("architect-project/play-pen/table-pane", tablePaneFactory);
		// factory will add the tablepanes to the playpen

		PPRelationshipFactory ppRelationshipFactory = new PPRelationshipFactory();
		d.addFactoryCreate("architect-project/play-pen/table-link", ppRelationshipFactory);
		
		DDLGeneratorFactory ddlgFactory = new DDLGeneratorFactory();
		d.addFactoryCreate("architect-project/ddl-generator", ddlgFactory);
		d.addSetProperties("architect-project/ddl-generator");


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
	protected class DBCSFactory extends AbstractObjectCreationFactory {
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
	 * Creates a SQLDatabase instance and adds it to the objectIdMap.
	 * Also attaches the DBCS referenced by the dbcsref attribute, if
	 * there is such an attribute.
	 */
	protected class SQLDatabaseFactory extends AbstractObjectCreationFactory {
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
	protected class SQLSchemaFactory extends AbstractObjectCreationFactory {
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
	protected class SQLTableFactory extends AbstractObjectCreationFactory {
		public Object createObject(Attributes attributes) {
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
	protected class SQLFolderFactory extends AbstractObjectCreationFactory {
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
	protected class SQLColumnFactory extends AbstractObjectCreationFactory {
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
	protected class SQLExceptionFactory extends AbstractObjectCreationFactory {
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
	protected class SQLRelationshipFactory extends AbstractObjectCreationFactory {
		public Object createObject(Attributes attributes) {
			SQLRelationship rel = new SQLRelationship();

			String id = attributes.getValue("id");
			if (id != null) {
				objectIdMap.put(id, rel);
			} else {
				logger.warn("No ID found in relationship element while loading project!");
			}

			String fkTableId = attributes.getValue("fk-table-ref");
			if (fkTableId != null) {
				SQLTable fkTable = (SQLTable) objectIdMap.get(fkTableId);
				rel.setFkTable(fkTable);
				try {
					fkTable.addImportedKey(rel);
				} catch (ArchitectException e) {
					logger.error("Couldn't add keys to table \""+fkTable.getName()+"\"", e);
					JOptionPane.showMessageDialog(null, "Failed to add f-keys to table:\n"+e.getMessage());
				}
			}

			String pkTableId = attributes.getValue("pk-table-ref");
			if (pkTableId != null) {
				SQLTable pkTable = (SQLTable) objectIdMap.get(pkTableId);
				rel.setPkTable(pkTable);
				try {
					pkTable.addExportedKey(rel);
				} catch (ArchitectException e) {
					logger.error("Couldn't add pk to table \""+pkTable.getName()+"\"", e);
					JOptionPane.showMessageDialog(null, "Failed to add pk to table:\n"+e.getMessage());
				}
			}

			return rel;
		}
	}

	/**
	 * Creates a ColumnMapping instance and adds it to the
	 * objectIdMap.  Also dereferences the fk-column-ref and
	 * pk-column-ref attributes if present.
	 */
	protected class ColumnMappingFactory extends AbstractObjectCreationFactory {
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

	protected class TablePaneFactory extends AbstractObjectCreationFactory {
		public Object createObject(Attributes attributes) {
			int x = Integer.parseInt(attributes.getValue("x"));
			int y = Integer.parseInt(attributes.getValue("y"));
			SQLTable tab = (SQLTable) objectIdMap.get(attributes.getValue("table-ref"));
			TablePane tp = new TablePane(tab, playPen);
			playPen.addTablePane(tp, new Point(x, y));
			return tp;
		}
	}

	protected class PPRelationshipFactory extends AbstractObjectCreationFactory {
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

	protected class DDLGeneratorFactory extends AbstractObjectCreationFactory {
		public Object createObject(Attributes attributes) {
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

	protected class FileFactory extends AbstractObjectCreationFactory {
		public Object createObject(Attributes attributes) {
			return new File(attributes.getValue("path"));
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
		
		try {
			// If creating this temp file fails, feed the user back a more explanatory message
			out = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
		} catch (IOException e) {
			throw new ArchitectException("Unable to create output file for save operation, data NOT saved.\n" + e, e);
		}
		
		objectIdMap = new HashMap();
		dbcsIdMap = new HashMap();
		indent = 0;
		progress = 0;
		boolean saveOk = false; // use this to determine if save process worked
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
		
		saveOk = save(out);	// Does ALL the actual I/O
		out = null;
		if (pm != null) 
			pm.close();
		pm = null;
		
		// Do the rename dance.
		// This is a REALLY bad place for failure (especially if we've made the user wait several hours to save
		// a large project), so we MUST check failures from renameto (both places!)
		if (saveOk) {
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
	}
	/**
	 * Do just the writing part of save, given a PrintWriter
	 * @param out - the file to write to
	 * @return True iff the save completed OK
	 * @throws IOException
	 * @throws ArchitectException
	 */
	public boolean save(PrintWriter out) throws IOException, ArchitectException {
		boolean saveOk;
		try {
			println(out, "<?xml version=\"1.0\"?>");
			println(out, "<architect-project version=\"0.1\">");
			indent++;
			println(out, "<project-name>"+ArchitectUtils.escapeXML(name)+"</project-name>");
			saveDataSources(out);
			saveSourceDatabases(out);
			saveTargetDatabase(out);
			saveDDLGenerator(out);
			savePlayPen(out);
			indent--;
			println(out, "</architect-project>");
			setModified(false);
			saveOk = true;
		} finally {
			if (out != null) out.close();
			
		}
		return saveOk;
	}
	
	

	protected int countSourceTables(SQLObject o) throws ArchitectException {
		if (o instanceof SQLTable) {
			return 1;
		} else if (o == playPen.getDatabase()) {
			return 0;
		} else if ( (!o.allowsChildren()) || !(o.isPopulated()) || o.getChildren() == null) {
		    return 0;
		} else {
			int myCount = 0;
			Iterator it = o.getChildren().iterator();
			while (it.hasNext()) {
				myCount += countSourceTables((SQLObject) it.next());
			}
			return myCount;
		}
	}

	protected void saveDataSources(PrintWriter out) throws IOException, ArchitectException {
		println(out, "<project-data-sources>");
		indent++;
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
				println(out, "<data-source id=\""+ArchitectUtils.escapeXML(id)+"\">");
				indent++;
				Iterator pit = ds.getPropertiesMap().entrySet().iterator();
				while (pit.hasNext()) {
				    Map.Entry ent = (Map.Entry) pit.next();
				    if (ent.getValue() != null) {
				        println(out, "<property key="+quote((String) ent.getKey())+" value="+quote((String) ent.getValue())+" />");
				    }
				}
				indent--;
				println(out, "</data-source>");
				dsNum++;
			}
			dsNum++;
		}
		indent--;
		println(out, "</project-data-sources>");
	}

	protected void saveDDLGenerator(PrintWriter out) throws IOException {
		print(out, "<ddl-generator"
			  +" type=\""+ddlGenerator.getClass().getName()+"\""
			  +" allow-connection=\""+ddlGenerator.getAllowConnection()+"\"");
		if (ddlGenerator.getTargetCatalog() != null) {
			niprint(out, " target-catalog=\""+ArchitectUtils.escapeXML(ddlGenerator.getTargetCatalog())+"\"");
		}
		if (ddlGenerator.getTargetSchema() != null) {
			niprint(out, " target-schema=\""+ArchitectUtils.escapeXML(ddlGenerator.getTargetSchema())+"\"");
		}
		niprint(out, ">");
		indent++;
		if (ddlGenerator.getFile() != null) {
			println(out, "<file path=\""+ArchitectUtils.escapeXML(ddlGenerator.getFile().getPath())+"\" />");
		}
		indent--;
		println(out, "</ddl-generator>");
	}

	/**
	 * Creates a &lt;source-databases&gt; element which contains zero
	 * or more &lt;database&gt; elements.
	 * @param out2 
	 */
	protected void saveSourceDatabases(PrintWriter out) throws IOException, ArchitectException {
		println(out, "<source-databases>");
		indent++;
		SQLObject dbTreeRoot = (SQLObject) sourceDatabases.getModel().getRoot();
		Iterator it = dbTreeRoot.getChildren().iterator();
		while (it.hasNext()) {
			SQLObject o = (SQLObject) it.next();
			if (o != playPen.getDatabase()) {
				saveSQLObject(out, o);
			}
		}
		indent--;
		println(out, "</source-databases>");
	}
	
	/**
	 * Recursively walks through the children of db, writing to the
	 * output file all SQLRelationship objects encountered.
	 */
	protected void saveRelationships(PrintWriter out, SQLDatabase db) throws ArchitectException, IOException {
		println(out, "<relationships>");
		indent++;
		Iterator it = db.getChildren().iterator();
		while (it.hasNext()) {
			saveRelationshipsRecurse(out, (SQLObject) it.next());
		}
		indent--;
		println(out, "</relationships>");
	}

	/**
	 * The recursive subroutine of saveRelationships.
	 */
	protected void saveRelationshipsRecurse(PrintWriter out, SQLObject o) throws ArchitectException, IOException {
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

	protected void saveTargetDatabase(PrintWriter out) throws IOException, ArchitectException {
		SQLDatabase db = (SQLDatabase) playPen.getDatabase();
		println(out, "<target-database dbcs-ref="+ quote(dbcsIdMap.get(db.getDataSource()).toString())+ ">");
		indent++;
		Iterator it = db.getChildren().iterator();
		while (it.hasNext()) {
			saveSQLObject(out, (SQLObject) it.next());
		}
		saveRelationships(out, db);
		indent--;
		println(out, "</target-database>");
	}
	
	protected void savePlayPen(PrintWriter out) throws IOException, ArchitectException {
		println(out, "<play-pen>");
		indent++;
		Iterator it = playPen.getTablePanes().iterator();
		while (it.hasNext()) {
			TablePane tp = (TablePane) it.next();
			Point p = tp.getLocation();
			println(out, "<table-pane table-ref="+quote(objectIdMap.get(tp.getModel()).toString())+""
					+" x=\""+p.x+"\" y=\""+p.y+"\" />");
			if (pm != null) {
			    pm.setProgress(++progress);
			    pm.setNote(tp.getModel().getShortDisplayName());
			}
		}

		it = playPen.getRelationships().iterator();
		while (it.hasNext()) {
			Relationship r = (Relationship) it.next();
			println(out, "<table-link relationship-ref="+quote(objectIdMap.get(r.getModel()).toString())
					+" pk-x=\""+r.getPkConnectionPoint().x+"\""
					+" pk-y=\""+r.getPkConnectionPoint().y+"\""
					+" fk-x=\""+r.getFkConnectionPoint().x+"\""
					+" fk-y=\""+r.getFkConnectionPoint().y+"\" />");
		}
		indent--;
		println(out, "</play-pen>");
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
	protected void saveSQLObject(PrintWriter out, SQLObject o) throws IOException, ArchitectException {
		String id = (String) objectIdMap.get(o);
		if (id != null) {
			println(out, "<reference ref-id=\""+ArchitectUtils.escapeXML(id)+"\" />");
			return;
		}

		String type;
		Map<String,Object> propNames = new TreeMap<String,Object>();
		
		// properties of all SQLObject types
		propNames.put("physicalName", o.getPhysicalName());
		// FIXME: refactor so we can put in the getName() here too
		
		if (o instanceof SQLDatabase) {
			id = "DB"+objectIdMap.size();
			type = "database";
			propNames.put("dbcs-ref", dbcsIdMap.get(((SQLDatabase) o).getDataSource()));
		} else if (o instanceof SQLCatalog) {
			id = "CAT"+objectIdMap.size();
			type = "catalog";
			propNames.put("catalogName", ((SQLCatalog) o).getCatalogName());
			propNames.put("nativeTerm", ((SQLCatalog) o).getNativeTerm());
		} else if (o instanceof SQLSchema) {
			id = "SCH"+objectIdMap.size();
			type = "schema";
			propNames.put("schemaName", ((SQLSchema) o).getSchemaName());
			propNames.put("nativeTerm", ((SQLSchema) o).getNativeTerm());
		} else if (o instanceof SQLTable) {
			id = "TAB"+objectIdMap.size();
			type = "table";
			propNames.put("tableName", ((SQLTable) o).getTableName());
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
			propNames.put("name", ((SQLTable.Folder) o).getName());
			propNames.put("type", new Integer(((SQLTable.Folder) o).getType()));
		} else if (o instanceof SQLColumn) {
			id = "COL"+objectIdMap.size();
			type = "column";
			SQLColumn sourceCol = ((SQLColumn) o).getSourceColumn();
			if (sourceCol != null) {
				propNames.put("source-column-ref", objectIdMap.get(sourceCol));
			}
			propNames.put("columnName", ((SQLColumn) o).getColumnName());
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
			propNames.put("name", ((SQLRelationship) o).getName());
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
		
		//print("<"+type+" hashCode=\""+o.hashCode()+"\" id=\""+id+"\" ");  // use this for debugging duplicate object problems
		print(out, "<"+type+" id="+quote(id)+" ");

		if (o.allowsChildren() && o.isPopulated() && o.getChildCount() == 1 && o.getChild(0) instanceof SQLExceptionNode) {
		    // if the only child is an exception node, just save the parent as non-populated
		    niprint(out, "populated=\"false\" ");
		    skipChildren = true;
		} else if ( (!savingEntireSource) && (!o.isPopulated()) ) {
			niprint(out, "populated=\"false\" ");
		} else {
		    niprint(out, "populated=\"true\" ");
		}

		Iterator props = propNames.keySet().iterator();
		while (props.hasNext()) {
			Object key = props.next();
			Object value = propNames.get(key);
			if (value != null) {
				niprint(out, key+"="+quote(value.toString())+" ");
			}
		}
		if ( (!skipChildren) && o.allowsChildren() && (savingEntireSource || o.isPopulated()) ) {
			niprintln(out, ">");
			Iterator children = o.getChildren().iterator();
			indent++;
			while (children.hasNext()) {
				SQLObject child = (SQLObject) children.next();
				if ( ! (child instanceof SQLRelationship)) {
					saveSQLObject(out, child);
				}
			}
			if (o instanceof SQLDatabase) {
				saveRelationships(out, (SQLDatabase) o);
			}
			indent--;
			println(out, "</"+type+">");
		} else {
			niprintln(out, "/>");
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
	 * Sets the value of target database in the PlayPen.
	 */
	public void setTargetDatabase(SQLDatabase db)  {
		playPen.setDatabase(db);
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
	 * Sets the value of playPen
	 *
	 * @param argPlayPen Value to assign to this.playPen
	 */
	public void setPlayPen(PlayPen argPlayPen) {
		this.playPen = argPlayPen;
		SwingUserSettings sprefs = ArchitectFrame.getMainInstance().sprefs;
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

	// ------------------- utility methods -------------------
	/**
	 * Prints to the output writer {@link #out} indentation spaces
	 * (according to {@link #indent}) followed by the given text.
	 * @param out 
	 */
	protected void print(PrintWriter out, String text) {
		for (int i = 0; i < indent; i++) {
			out.print(" ");
		}
		out.print(text);
	}

	/** 
	 * Prints <code>text</code> to the output writer {@link #out} (no
	 * indentation).
	 */
	protected void niprint(PrintWriter out, String text) {
		out.print(text);
	}

	/** 
	 * Prints <code>text</code> followed by newline to the output
	 * writer {@link #out} (no indentation).
	 */
	protected void niprintln(PrintWriter out, String text) {
		out.println(text);
	}

	/**
	 * Prints to the output writer {@link #out} indentation spaces
	 * (according to {@link #indent}) followed by the given text
	 * followed by a newline.
	 */
	protected void println(PrintWriter out, String text) {
		for (int i = 0; i < indent; i++) {
			out.print(" ");
		}
		out.println(text);
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
    
}
