package ca.sqlpower.architect.swingui;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.ArchitectTestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

/**
 * Test case, mainly for loading and saving via SwingUIProject.
 */
public class TestSwingUIProject extends ArchitectTestCase {
	
	private SwingUIProject project;
	private static final String ENCODING="UTF-8";
	private boolean deleteOnExit = false;
	
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
        "   <property key='Logical' value='Not Configured' />" +
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
        " <source-stuff datastoreTypeAsString='PROJECT' connectName='Arthur_test' " +
        " schema='ARCHITECT_REGRESS' filepath='' />"+
        "<target-stuff datastoreTypeAsString='FILE' filePath='Testpath' /> </compare-dm-settings>"+
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
	
    /**
     * Ensures the primary key property of columns loads properly. (from the example file)
     */
    public void testLoadPK() throws Exception {
        testLoad();
        
        SQLDatabase target = project.getTargetDatabase();
        
        SQLTable t1 = (SQLTable) target.getChild(0);
        assertEquals(1, t1.getPkSize());
        assertEquals(new Integer(0), t1.getColumn(0).getPrimaryKeySeq());
        assertNull(t1.getColumn(1).getPrimaryKeySeq());
    }
    
    private void subroutineForTestSaveLoadPK(SQLTable t) throws ArchitectException {
        assertEquals(2, t.getPkSize());
        assertEquals(new Integer(0), t.getColumn(0).getPrimaryKeySeq());
        assertEquals(new Integer(1), t.getColumn(1).getPrimaryKeySeq());
        assertEquals(null, t.getColumn(2).getPrimaryKeySeq());
        assertFalse(t.getColumn(0).isDefinitelyNullable());
        assertFalse(t.getColumn(1).isDefinitelyNullable());
        assertTrue(t.getColumn(0).isPrimaryKey());
        assertTrue(t.getColumn(1).isPrimaryKey());
        assertFalse(t.getColumn(2).isPrimaryKey());
    }
    
    /**
     * Ensures the primary key stuff of tables saves and loads properly.
     */
    public void testSaveLoadPK() throws Exception {
        // make a table with a pksize of 2
        SQLDatabase target = project.getTargetDatabase();
        SQLTable t = new SQLTable(null, "test_pk", null, "TABLE", true);
        t.addColumn(new SQLColumn(t, "pk1", Types.CHAR, 10, 0));
        t.addColumn(new SQLColumn(t, "pk2", Types.CHAR, 10, 0));
        t.addColumn(new SQLColumn(t, "nonpk", Types.CHAR, 10, 0));
        t.getColumn(0).setPrimaryKeySeq(0);
        t.getColumn(1).setPrimaryKeySeq(1);
        target.addChild(t);
        
        subroutineForTestSaveLoadPK(t);
        
        // save it
        File tmp = File.createTempFile("test", ".architect");
        if (deleteOnExit) {
            tmp.deleteOnExit();
        } else {
            System.out.println("testSaveLoadPK: tmp file is "+tmp);
        }
        PrintWriter out = new PrintWriter(tmp,ENCODING);
        assertNotNull(out);
        project.save(out,ENCODING);
        
        // load it back and check
        SwingUIProject project2 = new SwingUIProject("new test project");
        project2.load(new BufferedInputStream(new FileInputStream(tmp)));
        
        target = project2.getTargetDatabase();
        t = target.getTableByName("test_pk");
        subroutineForTestSaveLoadPK(t);
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
	public void testSave() throws Exception {
		testLoad();
 
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter out = new OutputStreamWriter(byteArrayOutputStream,ENCODING);
		assertNotNull(out);
		project.save(out,ENCODING);

        SwingUIProject p2 = new SwingUIProject("test2");
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toString().getBytes(ENCODING));
        p2.load(byteArrayInputStream);
        OutputStreamWriter out2 = new OutputStreamWriter(byteArrayOutputStream2,ENCODING);
		p2.save(out2,ENCODING);

        assertEquals(byteArrayOutputStream.toString(), byteArrayOutputStream2.toString());
	}
    
    /*
     * Test method for 'ca.sqlpower.architect.swingui.SwingUIProject.save(PrintWriter)'
     * Create two temp files, save our testData project to the first, load that
     * back in, and compare the names are the same.
     */
    public void testSavePersistsTablePanes() throws Exception {
        testLoad();
 
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter out = new OutputStreamWriter(byteArrayOutputStream,ENCODING);
        assertNotNull(out);
        project.save(out,ENCODING);
        System.out.println(byteArrayOutputStream.toString());
        SwingUIProject p2 = new SwingUIProject("test2");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toString().getBytes(ENCODING));
        p2.load(byteArrayInputStream);
        List<TablePane> projectTablePanes = project.getPlayPen().getTablePanes();
        List<TablePane> p2TablePanes = p2.getPlayPen().getTablePanes();
        assertEquals(projectTablePanes.size(),p2TablePanes.size());
        for (int i=0; i<projectTablePanes.size();i++){
            TablePane tp1 = projectTablePanes.get(i);
            TablePane tp2 = p2TablePanes.get(i);
            assertEquals("Wrong table names",tp1.getName(), tp2.getName());
        }
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
                } else if (props[i].getPropertyType() == SQLIndex.IndexType.class) {
                    if (oldVal != SQLIndex.IndexType.HASHED) {
                        newVal = SQLIndex.IndexType.HASHED;
                    } else {
                        newVal = SQLIndex.IndexType.CLUSTERED;
                    }
                } else if (props[i].getPropertyType() == SQLIndex.class) {
                    newVal = new SQLIndex();
                    ((SQLIndex) newVal).setName("a new index");
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
		propertiesToIgnore.add("playPenDatabase");  // only set by playpen code
		propertiesToIgnore.add("progressMonitor");
		propertiesToIgnore.add("zoomInAction");
		propertiesToIgnore.add("zoomOutAction");
        propertiesToIgnore.add("magicEnabled");
		
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
		ArchitectDataSourceType mockType = new ArchitectDataSourceType();
		ArchitectDataSource ds = new ArchitectDataSource();
        ds.setParentType(mockType);
		ds.setDisplayName("Schemaless Database");
		ds.getParentType().setJdbcDriver("ca.sqlpower.architect.MockJDBCDriver");
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
        propertiesToIgnore.add("magicEnabled");

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
        propertiesToIgnore.add("magicEnabled");

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
        propertiesToIgnore.add("magicEnabled");

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
        propertiesToIgnore.add("magicEnabled");

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
			System.out.println("testSaveCoversAllColumnProperties: temp file is "+tmp.getAbsolutePath());
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

    public void testSaveCoversAllIndexProperties() throws Exception {
        final String tableName = "rama_llama_dingdong";  // the power of the llama will save you
        testLoad();
        
        SQLDatabase ppdb = project.getPlayPen().getDatabase();
        SQLTable table = new SQLTable(ppdb, true);
        table.setName(tableName);
        SQLColumn col = new SQLColumn(table, "first", Types.VARCHAR, 10, 0);
        table.addColumn(col);
        SQLIndex target = new SQLIndex("testy index", false, null, null, null);
        target.addChild(target.new Column(col, false, false));
        ppdb.addChild(table);
        table.getIndicesFolder().addChild(target);
        col.setPrimaryKeySeq(0);
        
        Set<String> propertiesToIgnore = new HashSet<String>();
        propertiesToIgnore.add("SQLObjectListeners");
        propertiesToIgnore.add("undoEventListeners");
        propertiesToIgnore.add("magicEnabled");
        propertiesToIgnore.add("children");
        propertiesToIgnore.add("parent");
        propertiesToIgnore.add("class");

        Map<String,Object> oldDescription =
            setAllInterestingProperties(target, propertiesToIgnore);
        
        File tmp = File.createTempFile("test", ".architect");
        if (deleteOnExit) {
            tmp.deleteOnExit();
        } else {
            System.out.println("testSaveCoversAllIndexProperties: temp file is "+tmp.getAbsolutePath());
        }
        PrintWriter out = new PrintWriter(tmp,ENCODING);
        assertNotNull(out);
        project.save(out,ENCODING);
        
        
        InputStream in = new BufferedInputStream(new FileInputStream(tmp));
        StringBuffer sb = new StringBuffer(2000);
        int c;
        while( (c = in.read()) != -1) {
            sb.append((char)c);
        }
        System.out.println(sb.toString());
        in.close();

        
        
        SwingUIProject project2 = new SwingUIProject("new test project");
        project2.load(new BufferedInputStream(new FileInputStream(tmp)));
        
        ppdb = (SQLDatabase) project2.getPlayPen().getDatabase();
        
        SQLTable targetTable = (SQLTable) ppdb.getTableByName(tableName);
        System.out.println("target table=["+targetTable.getName()+"]");
        // child 1 because setPrimaryKeySeq calls normalizePrimaryKey who creates
        // a primary key is there is none made. The primary key is placed as child 0
        // in the list so it shows up first in the DBTree.
        target = (SQLIndex) (targetTable).getIndicesFolder().getChild(1);
        
        Map<String, Object> newDescription =
            getAllInterestingProperties(target, propertiesToIgnore);
        
        assertMapsEqual(oldDescription, newDescription);
    }

    public void testSaveCoversAllNonPKIndexColumnProperties() throws Exception {
        final String tableName = "delicatessen";
        testLoad();
        
        SQLDatabase ppdb = project.getPlayPen().getDatabase();
        SQLTable table = new SQLTable(ppdb, true);
        table.setName(tableName);
        SQLIndex index = new SQLIndex("tasty index", false, null, null, null);
        SQLIndex.Column indexCol = index.new Column("phogna bologna", false, true);
        ppdb.addChild(table);
        table.getIndicesFolder().addChild(index);
        index.addChild(indexCol);
        
        Set<String> propertiesToIgnore = new HashSet<String>();
        propertiesToIgnore.add("SQLObjectListeners");
        propertiesToIgnore.add("undoEventListeners");
        propertiesToIgnore.add("magicEnabled");
        propertiesToIgnore.add("children");
        propertiesToIgnore.add("parent");
        propertiesToIgnore.add("primaryKeyIndex");
        propertiesToIgnore.add("class");

        Map<String,Object> oldDescription =
            setAllInterestingProperties(index, propertiesToIgnore);
        propertiesToIgnore.remove("primaryKeyIndex");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream,ENCODING);
        project.save(writer,ENCODING);
        System.out.println(byteArrayOutputStream.toString());
        SwingUIProject project2 = new SwingUIProject("new test project");
        project2.load(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        
        // grab the second database in the dbtree's model (the first is the play pen)
        ppdb = (SQLDatabase) project2.getPlayPen().getDatabase();
        
        index = (SQLIndex) ((SQLTable) ppdb.getTableByName(tableName)).getIndicesFolder().getChild(0);
        
        Map<String, Object> newDescription =
            getAllInterestingProperties(index, propertiesToIgnore);
        
        assertMapsEqual(oldDescription, newDescription);
    }
    
    public void testSaveCoversAllPKColumnProperties() throws Exception {
        final String tableName = "delicatessen";
        testLoad();
        
        SQLDatabase ppdb = project.getPlayPen().getDatabase();
        SQLTable table = new SQLTable(ppdb, true);
        SQLColumn col = new SQLColumn(table,"col",1,0,0);
        table.setName(tableName);
        table.addColumn(col);
        SQLIndex index = new SQLIndex("tasty index", false, null, null, null);
        SQLIndex.Column indexCol = index.new Column(col, false, true);
        index.setPrimaryKeyIndex(true);
        ppdb.addChild(table);
        table.getIndicesFolder().addChild(index);
        index.addChild(indexCol);
        col.setPrimaryKeySeq(new Integer(0));
        
        Set<String> propertiesToIgnore = new HashSet<String>();
        propertiesToIgnore.add("SQLObjectListeners");
        propertiesToIgnore.add("undoEventListeners");
        propertiesToIgnore.add("magicEnabled");
        propertiesToIgnore.add("children");
        propertiesToIgnore.add("parent");
        propertiesToIgnore.add("primaryKeyIndex");
        propertiesToIgnore.add("class");

        Map<String,Object> oldDescription =
            setAllInterestingProperties(index, propertiesToIgnore);
        propertiesToIgnore.remove("primaryKeyIndex");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream,ENCODING);
        project.save(writer,ENCODING);
        System.out.println(byteArrayOutputStream.toString());
        SwingUIProject project2 = new SwingUIProject("new test project");
        project2.load(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        
        // grab the second database in the dbtree's model (the first is the play pen)
        ppdb = (SQLDatabase) project2.getPlayPen().getDatabase();
        System.out.println(ppdb.getTableByName(tableName));
        index = (SQLIndex) ((SQLTable) ppdb.getTableByName(tableName)).getIndicesFolder().getChild(0);
        
        Map<String, Object> newDescription =
            getAllInterestingProperties(index, propertiesToIgnore);
        
        assertMapsEqual(oldDescription, newDescription);
    }
    
    public void testSaveIndexColumnPointingToAColumn() throws Exception {
        final String tableName = "delicatessen";
        testLoad();
        
        SQLDatabase ppdb = project.getPlayPen().getDatabase();
        SQLTable table = new SQLTable(ppdb, true);
        SQLColumn col = new SQLColumn(table,"Column 1",1,1,1);
        table.addColumn(col);
        table.setName(tableName);
        SQLIndex index = new SQLIndex("tasty index", false, null, null, null);
        index.addIndexColumn(col, false, true);
        ppdb.addChild(table);
        table.getIndicesFolder().addChild(index);
        col.setPrimaryKeySeq(new Integer(0));
        Set<String> propertiesToIgnore = new HashSet<String>();
        propertiesToIgnore.add("SQLObjectListeners");
        propertiesToIgnore.add("undoEventListeners");
        propertiesToIgnore.add("magicEnabled");
        propertiesToIgnore.add("children");
        propertiesToIgnore.add("parent");
        propertiesToIgnore.add("class");

        Map<String,Object> oldDescription =
            setAllInterestingProperties(index, propertiesToIgnore);
        
        File tmp = File.createTempFile("test", ".architect");
        if (deleteOnExit) {
            tmp.deleteOnExit();
        } else {
            System.out.println("testSaveCoversAllIndexProperties: temp file is "+tmp.getAbsolutePath());
        }
        PrintWriter out = new PrintWriter(tmp,ENCODING);
        assertNotNull(out);
        project.save(out,ENCODING);
        
        SwingUIProject project2 = new SwingUIProject("new test project");
        project2.load(new BufferedInputStream(new FileInputStream(tmp)));
        
        // grab the second database in the dbtree's model (the first is the play pen)
        ppdb = (SQLDatabase) project2.getPlayPen().getDatabase();
        
        // child 1 because setPrimaryKeySeq calls normalizePrimaryKey which creates
        // a primary key is there is none made. The primary key is placed as child 0
        // in the list so it shows up first in the DBTree.
        index = (SQLIndex) ((SQLTable) ppdb.getTableByName(tableName)).getIndicesFolder().getChild(1);
        
        Map<String, Object> newDescription =
            getAllInterestingProperties(index, propertiesToIgnore);
        
        assertMapsEqual(oldDescription, newDescription);
    }

	public void testNotModifiedWhenFreshlyLoaded() throws Exception {
		testLoad();
		assertFalse("Freshly loaded project should not be marked dirty",
				project.isModified());
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
		assertEquals("PROJECT", cds.getSourceSettings().getDatastoreType().toString());
		assertEquals("Arthur_test", cds.getSourceSettings().getConnectName());
		assertEquals("ARCHITECT_REGRESS", cds.getSourceSettings().getSchema());
		assertEquals("FILE", cds.getTargetSettings().getDatastoreType().toString());
		assertEquals("Testpath", cds.getTargetSettings().getFilePath());				
	}
}