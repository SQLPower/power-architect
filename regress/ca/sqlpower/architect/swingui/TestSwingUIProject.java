package regress.ca.sqlpower.architect.swingui;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.beanutils.PropertyUtils;

import regress.ArchitectTestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.CompareDMSettings;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.DBTreeModel;
import ca.sqlpower.architect.swingui.SwingUIProject;

/**
 * Test case, mainly for loading and saving via SwingUIProject.
 */
public class TestSwingUIProject extends ArchitectTestCase {
	
	private SwingUIProject project;
	private static final String ENCODING="UTF-8";
	private boolean deleteOnExit = true;
	
	/*
	 * Test method for 'ca.sqlpower.architect.swingui.SwingUIProject.SwingUIProject(String)'
	 */
	public void setUp() throws Exception {
		project = new SwingUIProject("test");
	}

	private final String testData =
        "<?xml version='1.0'?>" +
        "<architect-project version='0.1'>" +
        " <project-name>TestSwingUIProject</project-name>" +
        " <project-data-sources>" +
        "  <data-source id='DS0'>" +
        "   <property key='Logical' value='Target Database' />" +
        "  </data-source>" +
        " </project-data-sources>" +
        " <source-databases>" +
        " </source-databases>" +
        " <target-database dbcs-ref='DS0'>" +
        "  <table id='TAB0' populated='true' primaryKeyName='id' remarks='' name='Customers' >" +
        "   <folder id='FOL1' populated='true' name='Columns' type='1' >" +
        "    <column id='COL2' populated='true' autoIncrement='false' name='id' defaultValue='' nullable='0' precision='10' primaryKeySeq='0' referenceCount='1' remarks='' scale='0' type='4' />" +
        "    <column id='COL3' populated='true' autoIncrement='false' name='name' defaultValue='' nullable='0' precision='10' referenceCount='1' remarks='' scale='0' type='4' />" +
        "   </folder>" +
        "   <folder id='FOL4' populated='true' name='Exported Keys' type='3' >" +
        "   </folder>" +
        "   <folder id='FOL5' populated='true' name='Imported Keys' type='2' >" +
        "   </folder>" +
        "  </table>" +
        "  <table id='TAB6' populated='true' primaryKeyName='id' remarks='' name='Orders' >" +
        "   <folder id='FOL7' populated='true' name='Columns' type='1' >" +
        "    <column id='COL8' populated='true' autoIncrement='false' name='i&amp;d' defaultValue='' " +
        "    remarks=\"This isn't a problem\" nullable='0' precision='10' primaryKeySeq='0' referenceCount='1' scale='0' type='4' />" +
        "    <column id='COL9' populated='true' autoIncrement='false' name='customer&lt;id' defaultValue='' nullable='0' precision='10' referenceCount='1' remarks='' scale='0' type='4' />" +
        "   </folder>" +
        "   <folder id='FOL10' populated='true' name='Exported Keys' type='3' >" +
        "   </folder>" +
        "   <folder id='FOL11' populated='true' name='Imported Keys' type='2' >" +
        "   </folder>" +
        "  </table>" +
        "  <relationships>" +
        "   <relationship id='REL12' populated='true' deferrability='0' deleteRule='0' fk-table-ref='TAB0' fkCardinality='6' identifying='true' name='Orders_Customers_fk' pk-table-ref='TAB6' pkCardinality='2' updateRule='0' >" +
        "    <column-mapping id='CMP13' populated='true' fk-column-ref='COL2' pk-column-ref='COL8' />" +
        "   </relationship>" +
        "   <reference ref-id='REL12' />" +
        "  </relationships>" +
        " </target-database>" +
        " <ddl-generator type='ca.sqlpower.architect.ddl.GenericDDLGenerator' allow-connection='true'> </ddl-generator>" + 
        " <compare-dm-settings sqlScriptFormat='SQLServer 2000' outputFormatAsString='ENGLISH'>" +        
        " <source-stuff radioButtonSelectionAsString='PROJECT' connectName='Arthur_test' " +
        " schema='ARCHITECT_REGRESS' filepath='' />"+
        "<target-stuff radioButtonSelectionAsString='FILE' filePath='Testpath' /> </compare-dm-settings>"+
        " <play-pen>" +
        "  <table-pane table-ref='TAB0' x='85' y='101' />" +
        "  <table-pane table-ref='TAB6' x='196' y='38' />" +
        "  <table-link relationship-ref='REL12' pk-x='76' pk-y='60' fk-x='114' fk-y='30' />" +
        " </play-pen>" +
        "</architect-project>";
	
	/*
	 * Test method for 'ca.sqlpower.architect.swingui.SwingUIProject.load(InputStream)'
	 */
	public void testLoad() throws Exception {
		// StringReader r = new StringReader(testData);
		ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());
		project.load(r);
		assertFalse("Project starts out with undo history",project.getUndoManager().canUndoOrRedo());

		DBTree tree = project.getSourceDatabases();
		assertNotNull(tree);
		assertEquals(tree.getComponentCount(), 1 );
		
		SQLDatabase target = project.getTargetDatabase(); 
		assertNotNull(target);
		
		assertEquals(target.getName(), "Not Configured");
		assertEquals(target.getChildCount(), 2);		
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.swingui.SwingUIProject.save(ProgressMonitor)'
	 */
	public void testSaveProgressMonitor() throws Exception {
		System.out.println("TestSwingUIProject.testSaveProgressMonitor()");
		MockProgressMonitor mockProgressMonitor = new MockProgressMonitor(null, "Hello", "Hello again", 0, 100);
		File file = File.createTempFile("test", "architect");
		project.setFile(file);
		project.save(mockProgressMonitor);
		
		SwingUIProject p2 = new SwingUIProject("test2");
		p2.load(new FileInputStream(file));
		File tmp2 = File.createTempFile("test2", ".architect");
		if (deleteOnExit) {
			tmp2.deleteOnExit();
		}
		p2.save(new PrintWriter(tmp2,ENCODING),ENCODING);
		assertEquals(file.length(), tmp2.length());	// Quick test
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.swingui.SwingUIProject.save(PrintWriter)'
	 * Create two temp files, save our testData project to the first, load that
	 * back in, save it to the second, and compare the two temp files.
	 */
	public void testSavePrintWriter() throws Exception {
		testLoad();
		File tmp = File.createTempFile("test", ".architect");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		project.save(out,ENCODING);
		
		SwingUIProject p2 = new SwingUIProject("test2");
		p2.load(new FileInputStream(tmp));
		File tmp2 = File.createTempFile("test2", ".architect");
		if (deleteOnExit) {
			tmp2.deleteOnExit();
		}
		p2.save(new PrintWriter(tmp2,ENCODING),ENCODING);
		assertEquals(tmp.length(), tmp2.length());	// Quick test
	}

	/** Save a document and use built-in JAXP to ensure it is at least well-formed XML.
	 * @throws Exception
	 */
	public void testSaveIsWellFormed() throws Exception {
		boolean validate = false;
		testLoad();
		File tmp = File.createTempFile("test", ".architect");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		
		project.setName("FOO<BAR");		// Implicitly testing sanitizeXML method here!
		
		project.save(out,ENCODING);
		
		System.err.println("Parsing " + tmp + "...");

		// Make the document a URL so relative DTD works.
		String uri = "file:" + tmp.getAbsolutePath();

		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		if (validate)
			f.setValidating(true);
		DocumentBuilder p = f.newDocumentBuilder();
		p.parse(uri);
		System.out.println("Parsed OK");
	}
	
	/**
	 * Sets all the settable properties on the given target object
	 * which are not in the given ignore set.
	 * 
	 * @param target The object to change the properties of
	 * @param propertiesToIgnore The properties of target not to modify or read
	 * @return A Map describing the new values of all the non-ignored, readable 
	 * properties in target.
	 */
	private static Map<String,Object> setAllInterestingProperties(SQLObject target,
			Set<String> propertiesToIgnore) throws Exception {
		
		PropertyDescriptor props[] = PropertyUtils.getPropertyDescriptors(target);
		for (int i = 0; i < props.length; i++) {
			Object oldVal = null;
			if (PropertyUtils.isReadable(target, props[i].getName()) &&
					props[i].getReadMethod() != null &&
					!propertiesToIgnore.contains(props[i].getName())) {
				oldVal = PropertyUtils.getProperty(target, props[i].getName());
			}
			if (PropertyUtils.isWriteable(target, props[i].getName()) &&
					props[i].getWriteMethod() != null &&
					!propertiesToIgnore.contains(props[i].getName())) {
				
				// XXX: factor this (and the same thing in SQLTestCase) 
				//      out into a changeValue() method in some util class.
				
				Object newVal;  // don't init here so compiler can warn if the following code doesn't always give it a value
				if (props[i].getPropertyType() == Integer.TYPE) {
					newVal = ((Integer)oldVal)+1;
				} else if (props[i].getPropertyType() == Integer.class) {
					if (oldVal == null) {
						newVal = new Integer(1);
					} else {
						newVal = new Integer((Integer)oldVal+1);
					}
				} else if (props[i].getPropertyType() == String.class) {
					// make sure it's unique
					newVal ="new " + oldVal;
				} else if (props[i].getPropertyType() == Boolean.TYPE){
					newVal = new Boolean(! ((Boolean) oldVal).booleanValue());
				} else if (props[i].getPropertyType() == SQLColumn.class) {
					newVal = new SQLColumn();
					((SQLColumn) newVal).setName("testing!");
				} else {
					throw new RuntimeException("This test case lacks a value for "+
							props[i].getName()+
							" (type "+props[i].getPropertyType().getName()+")");
				}

				PropertyUtils.setProperty(target, props[i].getName(), newVal);
			}
		}
		
		// read them all back at the end in case there were dependencies between properties
		return getAllInterestingProperties(target, propertiesToIgnore);
	}
	
	/**
	 * Gets all the settable properties on the given target object
	 * which are not in the given ignore set, and stuffs them into a Map.
	 * 
	 * @param target The object to change the properties of
	 * @param propertiesToIgnore The properties of target not to modify or read
	 * @return The aforementioned stuffed map
	 */
	private static Map<String, Object> getAllInterestingProperties(SQLObject target, Set<String> propertiesToIgnore) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String,Object> newDescription = new HashMap<String,Object>();
		PropertyDescriptor[] props = PropertyUtils.getPropertyDescriptors(target);
		for (int i = 0; i < props.length; i++) {
			if (PropertyUtils.isReadable(target, props[i].getName()) &&
					props[i].getReadMethod() != null &&
					!propertiesToIgnore.contains(props[i].getName())) {
				newDescription.put(props[i].getName(),
						PropertyUtils.getProperty(target, props[i].getName()));
			}
		}
		return newDescription;
	}
	
	public void testSaveCoversAllDatabaseProperties() throws Exception {
		testLoad();
		DBTree dbTree = project.getSourceDatabases();
		DBTreeModel dbTreeModel = (DBTreeModel) dbTree.getModel();
		
		ArchitectDataSource fakeDataSource = new ArchitectDataSource();
		SQLDatabase db = new SQLDatabase() {
			@Override
			public Connection getConnection() throws ArchitectException {
				return null;
			}
		};
		db.setDataSource(fakeDataSource);
		db.setPopulated(true);
		((SQLObject) dbTreeModel.getRoot()).addChild(db);
		
		Set<String> propertiesToIgnore = new HashSet<String>();
		propertiesToIgnore.add("SQLObjectListeners");
		propertiesToIgnore.add("children");
		propertiesToIgnore.add("tables");
		propertiesToIgnore.add("parent");
		propertiesToIgnore.add("parentDatabase");
		propertiesToIgnore.add("class");
		propertiesToIgnore.add("childCount");
		propertiesToIgnore.add("connection");
		propertiesToIgnore.add("populated");
		propertiesToIgnore.add("secondaryChangeMode");
		propertiesToIgnore.add("dataSource");  // we set this already!
		propertiesToIgnore.add("ignoreReset");  // only used (and set) by playpen code
		propertiesToIgnore.add("progressMonitor");
		
		Map<String,Object> oldDescription =
			setAllInterestingProperties(db, propertiesToIgnore);
		
		
		File tmp = File.createTempFile("test", ".architect");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		project.save(out,ENCODING);
		
		SwingUIProject project2 = new SwingUIProject("new test project");
		project2.load(new BufferedInputStream(new FileInputStream(tmp)));
		
		// grab the second database in the dbtree's model (the first is the play pen)
		db = (SQLDatabase) project2.getSourceDatabases().getDatabaseList().get(1);
		
		Map<String, Object> newDescription =
			getAllInterestingProperties(db, propertiesToIgnore);
		
		assertEquals("loaded-in version of database doesn't match the original!",
				oldDescription.toString(), newDescription.toString());
	}
	

	public void testSaveCoversAllCatalogProperties() throws Exception {
		
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setDisplayName("Schemaless Database");
		ds.setDriverClass("regress.ca.sqlpower.architect.MockJDBCDriver");
		ds.setUser("fake");
		ds.setPass("fake");
		//this creates a mock jdbc database with only catalogs
		ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&catalogs=cat1,cat2,cat3");
		
		DBTree dbTree;
		DBTreeModel dbTreeModel = null;
		
		testLoad();
		dbTree = project.getSourceDatabases();
		dbTreeModel = (DBTreeModel) dbTree.getModel();
		
		SQLDatabase db = new SQLDatabase();
		db.setDataSource(ds);
		db.setPopulated(true);
		
		((SQLObject) dbTreeModel.getRoot()).addChild(db);
	
		SQLCatalog target = new SQLCatalog(db, "my test catalog");
		db.addChild(target);
		
		Set<String> propertiesToIgnore = new HashSet<String>();
		propertiesToIgnore.add("SQLObjectListeners");
		propertiesToIgnore.add("children");
		propertiesToIgnore.add("parent");
		propertiesToIgnore.add("parentDatabase");
		propertiesToIgnore.add("class");
		propertiesToIgnore.add("childCount");
		propertiesToIgnore.add("secondaryChangeMode");
		propertiesToIgnore.add("populated");

		Map<String,Object> oldDescription =
			setAllInterestingProperties(target, propertiesToIgnore);
		
		
		File tmp = File.createTempFile("test", ".architect");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		project.save(out,ENCODING);
		
		SwingUIProject project2 = new SwingUIProject("new test project");
		project2.load(new BufferedInputStream(new FileInputStream(tmp)));
		
		// grab the second database in the dbtree's model (the first is the play pen)
		db = (SQLDatabase) project2.getSourceDatabases().getDatabaseList().get(1);
		
		target = (SQLCatalog) db.getChild(0);
		
		Map<String, Object> newDescription =
			getAllInterestingProperties(target, propertiesToIgnore);
		
		assertMapsEqual(oldDescription, newDescription);
	}

	public void testSaveCoversAllSchemaProperties() throws Exception {
		testLoad();
		DBTree dbTree = project.getSourceDatabases();
		DBTreeModel dbTreeModel = (DBTreeModel) dbTree.getModel();
		
		ArchitectDataSource fakeDataSource = new ArchitectDataSource();
		SQLDatabase db = new SQLDatabase();
		db.setDataSource(fakeDataSource);
		db.setPopulated(true);
		((SQLObject) dbTreeModel.getRoot()).addChild(db);
		
		SQLSchema target = new SQLSchema(db, "my test schema", true);
		db.addChild(target);
		
		Set<String> propertiesToIgnore = new HashSet<String>();
		propertiesToIgnore.add("SQLObjectListeners");
		propertiesToIgnore.add("children");
		propertiesToIgnore.add("parent");
		propertiesToIgnore.add("parentDatabase");
		propertiesToIgnore.add("class");
		propertiesToIgnore.add("childCount");
		propertiesToIgnore.add("populated");
		propertiesToIgnore.add("secondaryChangeMode");

		Map<String,Object> oldDescription =
			setAllInterestingProperties(target, propertiesToIgnore);
		
		
		File tmp = File.createTempFile("test", ".architect");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		project.save(out,ENCODING);
		
		SwingUIProject project2 = new SwingUIProject("new test project");
		project2.load(new BufferedInputStream(new FileInputStream(tmp)));
		
		// grab the second database in the dbtree's model (the first is the play pen)
		db = (SQLDatabase) project2.getSourceDatabases().getDatabaseList().get(1);
		
		target = (SQLSchema) db.getChild(0);
		
		Map<String, Object> newDescription =
			getAllInterestingProperties(target, propertiesToIgnore);
		
		assertMapsEqual(oldDescription, newDescription);
	}

	public void testSaveCoversAllTableProperties() throws Exception {
		testLoad();
		DBTree dbTree = project.getSourceDatabases();
		DBTreeModel dbTreeModel = (DBTreeModel) dbTree.getModel();
		
		ArchitectDataSource fakeDataSource = new ArchitectDataSource();
		SQLDatabase db = new SQLDatabase();
		db.setDataSource(fakeDataSource);
		db.setPopulated(true);
		((SQLObject) dbTreeModel.getRoot()).addChild(db);
		
		SQLTable target = new SQLTable(db, true);
		db.addChild(target);
		
		Set<String> propertiesToIgnore = new HashSet<String>();
		propertiesToIgnore.add("SQLObjectListeners");
		propertiesToIgnore.add("children");
		propertiesToIgnore.add("parent");
		propertiesToIgnore.add("parentDatabase");
		propertiesToIgnore.add("class");
		propertiesToIgnore.add("childCount");
		propertiesToIgnore.add("populated");
		propertiesToIgnore.add("columnsFolder");
		propertiesToIgnore.add("secondaryChangeMode");

		Map<String,Object> oldDescription =
			setAllInterestingProperties(target, propertiesToIgnore);
		
		
		File tmp = File.createTempFile("test", ".architect");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		project.save(out,ENCODING);
		
		SwingUIProject project2 = new SwingUIProject("new test project");
		project2.load(new BufferedInputStream(new FileInputStream(tmp)));
		
		// grab the second database in the dbtree's model (the first is the play pen)
		db = (SQLDatabase) project2.getSourceDatabases().getDatabaseList().get(1);
		
		target = (SQLTable) db.getChild(0);
		
		Map<String, Object> newDescription =
			getAllInterestingProperties(target, propertiesToIgnore);
		
		assertMapsEqual(oldDescription, newDescription);
	}

	public void testSaveCoversAllColumnProperties() throws Exception {
		final String tableName = "harry";
		testLoad();
		
		SQLDatabase ppdb = project.getPlayPen().getDatabase();
		SQLTable table = new SQLTable(ppdb, true);
		table.setName(tableName);
		SQLColumn target = new SQLColumn(table, "my cool test column", Types.INTEGER, 10, 10);
		ppdb.addChild(table);
		table.addColumn(target);
		
		Set<String> propertiesToIgnore = new HashSet<String>();
		propertiesToIgnore.add("SQLObjectListeners");
		propertiesToIgnore.add("children");
		propertiesToIgnore.add("parent");
		propertiesToIgnore.add("parentTable");
		propertiesToIgnore.add("class");
		propertiesToIgnore.add("childCount");
		propertiesToIgnore.add("populated");
		propertiesToIgnore.add("undoEventListeners");
		propertiesToIgnore.add("secondaryChangeMode");

		Map<String,Object> oldDescription =
			setAllInterestingProperties(target, propertiesToIgnore);
		
		// need to set sourceColumn manually because it has to exist in the database.
		{
			// different variable scope
			DBTree dbTree = project.getSourceDatabases();
			DBTreeModel dbTreeModel = (DBTreeModel) dbTree.getModel();
			
			ArchitectDataSource fakeDataSource = new ArchitectDataSource();
			SQLDatabase db = new SQLDatabase();
			db.setDataSource(fakeDataSource);
			db.setPopulated(true);
			((SQLObject) dbTreeModel.getRoot()).addChild(db);
			
			SQLTable sourceTable = new SQLTable(db, true);
			SQLColumn sourceColumn = new SQLColumn(sourceTable, "my cool source column", Types.INTEGER, 10, 10);
			sourceTable.addColumn(sourceColumn);
			db.addChild(sourceTable);

			// make sure target has a source column that can be saved in the project
			target.setSourceColumn(sourceColumn);
			oldDescription.put("sourceColumn", sourceColumn);
		}
		
		File tmp = File.createTempFile("test", ".architect");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		} else {
			System.out.println("MY TEMP FILE: "+tmp.getAbsolutePath());
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		project.save(out,ENCODING);
		
		SwingUIProject project2 = new SwingUIProject("new test project");
		project2.load(new BufferedInputStream(new FileInputStream(tmp)));
		
		// grab the second database in the dbtree's model (the first is the play pen)
		ppdb = (SQLDatabase) project2.getPlayPen().getDatabase();
		
		target = ((SQLTable) ppdb.getTableByName(tableName)).getColumn(0);
		
		Map<String, Object> newDescription =
			getAllInterestingProperties(target, propertiesToIgnore);
		
		assertMapsEqual(oldDescription, newDescription);
	}

	public void testNotModifiedWhenFreshlyLoaded() throws Exception {
		testLoad();
		assertFalse("Freshly loaded project should not be marked dirty",
				project.isModified());
	}
	
	public void testSetModified() {
		// TODO: implement test
	}
	
	public void testSaveCoversCompareDMSettings() throws Exception {
		testLoad();
		CompareDMSettings cds = project.getCompareDMSettings();		
		File tmp = File.createTempFile("test", ".architect");
		assertFalse (cds.getSaveFlag());
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		project.save(out,ENCODING);
		assertFalse (cds.getSaveFlag());		
		assertEquals("SQLServer 2000", cds.getSqlScriptFormat());
		assertEquals("ENGLISH", cds.getOutputFormatAsString());
		assertEquals("PROJECT", cds.getSourceSettings().getButtonSelection().toString());
		assertEquals("Arthur_test", cds.getSourceSettings().getConnectName());
		assertEquals("ARCHITECT_REGRESS", cds.getSourceSettings().getSchema());
		assertEquals("FILE", cds.getTargetSettings().getButtonSelection().toString());
		assertEquals("Testpath", cds.getTargetSettings().getFilePath());				
	}
}
