package regress.ca.sqlpower.architect.swingui;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.DBTreeModel;
import ca.sqlpower.architect.swingui.SwingUIProject;

/**
 * Test case, mainly for loading and saving via SwingUIProject.
 */
public class TestSwingUIProject extends TestCase {
	
	private SwingUIProject project;
	
	private final boolean deleteOnExit = true;
	
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
        "  <table id='TAB0' populated='true' primaryKeyName='id' remarks='' tableName='Customers' >" +
        "   <folder id='FOL1' populated='true' name='Columns' type='1' >" +
        "    <column id='COL2' populated='true' autoIncrement='false' columnName='id' defaultValue='' nullable='0' precision='10' primaryKeySeq='0' referenceCount='1' remarks='' scale='0' type='4' />" +
        "    <column id='COL3' populated='true' autoIncrement='false' columnName='name' defaultValue='' nullable='0' precision='10' referenceCount='1' remarks='' scale='0' type='4' />" +
        "   </folder>" +
        "   <folder id='FOL4' populated='true' name='Exported Keys' type='3' >" +
        "   </folder>" +
        "   <folder id='FOL5' populated='true' name='Imported Keys' type='2' >" +
        "   </folder>" +
        "  </table>" +
        "  <table id='TAB6' populated='true' primaryKeyName='id' remarks='' tableName='Orders' >" +
        "   <folder id='FOL7' populated='true' name='Columns' type='1' >" +
        "    <column id='COL8' populated='true' autoIncrement='false' columnName='i&amp;d' defaultValue='' " +
        "    remarks=\"This isn't a problem\" nullable='0' precision='10' primaryKeySeq='0' referenceCount='1' scale='0' type='4' />" +
        "    <column id='COL9' populated='true' autoIncrement='false' columnName='customer&lt;id' defaultValue='' nullable='0' precision='10' referenceCount='1' remarks='' scale='0' type='4' />" +
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
		System.out.println("TestSwingUIProject.testLoad(): Project Loaded OK");
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
		p2.save(new PrintWriter(tmp2));
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
		PrintWriter out = new PrintWriter(tmp);
		assertNotNull(out);
		project.save(out);
		
		SwingUIProject p2 = new SwingUIProject("test2");
		p2.load(new FileInputStream(tmp));
		File tmp2 = File.createTempFile("test2", ".architect");
		if (deleteOnExit) {
			tmp2.deleteOnExit();
		}
		p2.save(new PrintWriter(tmp2));
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
		PrintWriter out = new PrintWriter(tmp);
		assertNotNull(out);
		
		project.setName("FOO<BAR");		// Implicitly testing sanitizeXML method here!
		
		project.save(out);
		
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
		
		Map<String,Object> description = new HashMap<String,Object>();
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
				} else if (props[i].getPropertyType() == String.class) {
					// make sure it's unique
					newVal ="new " + oldVal;
				} else if (props[i].getPropertyType() == Boolean.TYPE){
					newVal = new Boolean(! ((Boolean) oldVal).booleanValue());
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
		propertiesToIgnore.add("dataSource");  // we set this already!
		propertiesToIgnore.add("ignoreReset");  // only used (and set) by playpen code
		propertiesToIgnore.add("progressMonitor");
		
		Map<String,Object> oldDescription =
			setAllInterestingProperties(db, propertiesToIgnore);
		
		
		File tmp = File.createTempFile("test", ".architect");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp);
		assertNotNull(out);
		project.save(out);
		
		SwingUIProject project2 = new SwingUIProject("new test project");
		project2.load(new BufferedInputStream(new FileInputStream(tmp)));
		
		// grab the second database in the dbtree's model (the first is the play pen)
		db = (SQLDatabase) project2.getSourceDatabases().getDatabaseList().get(1);
		
		Map<String, Object> newDescription =
			getAllInterestingProperties(db, propertiesToIgnore);
		
		assertEquals("loaded-in version of database doesn't match the original!",
				oldDescription, newDescription);
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

		Map<String,Object> oldDescription =
			setAllInterestingProperties(target, propertiesToIgnore);
		
		
		File tmp = File.createTempFile("test", ".architect");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp);
		assertNotNull(out);
		project.save(out);
		
		SwingUIProject project2 = new SwingUIProject("new test project");
		project2.load(new BufferedInputStream(new FileInputStream(tmp)));
		
		// grab the second database in the dbtree's model (the first is the play pen)
		db = (SQLDatabase) project2.getSourceDatabases().getDatabaseList().get(1);
		
		target = (SQLTable) db.getChild(0);
		
		Map<String, Object> newDescription =
			getAllInterestingProperties(target, propertiesToIgnore);
		
		myAssertMapsEqual(oldDescription, newDescription);
	}
	
	public static void myAssertMapsEqual(Map<String,Object> expected,
			Map<String,Object> actual) throws AssertionFailedError {
		StringBuffer errors = new StringBuffer();
		for (Map.Entry<String,Object> expectedEntry : expected.entrySet()) {
			Object actualValue = actual.get(expectedEntry.getKey());
			if (expectedEntry.getValue() == null) {
				// skip this check (we don't save null-valued properties)
			} else if (actualValue == null) {
				errors.append("Expected entry '"+expectedEntry.getKey()+
						"' missing in actual value map (expected value: '"
						+expectedEntry.getValue()+"')\n");
			} else if ( ! actualValue.equals(expectedEntry.getValue())) {
				errors.append("Value of '"+expectedEntry.getKey()+
						"' differs (expected: '"+expectedEntry.getValue()+
						"'; actual: '"+actualValue+"')\n");
			}
		}
		assertFalse(errors.toString(), errors.length() > 0);
	}

	public void testGetName() {
		// TODO: implement test
	}
	
	public void testSetName() {
		// TODO: implement test
	}
	
	public void testGetSourceDatabases() {
		// TODO: implement test
	}
	
	public void testSetSourceDatabases() {
		// TODO: implement test
	}
	
	public void testSetSourceDatabaseList() {
		// TODO: implement test
	}
	
	public void testGetTargetDatabase() {
		// TODO: implement test
	}
	
	public void testSetTargetDatabase() {
		// TODO: implement test
	}
	
	public void testGetFile() {
		// TODO: implement test
	}
	
	public void testSetFile() {
		// TODO: implement test
	}
	
	public void testGetPlayPen() {
		// TODO: implement test
	}
	
	public void testSetPlayPen() {
		// TODO: implement test
	}
	
	public void testGetDDLGenerator() {
		// TODO: implement test
	}
	
	public void testSetDDLGenerator() {
		// TODO: implement test
	}
	
	public void testIsSavingEntireSource() {
		// TODO: implement test
	}
	
	public void testSetSavingEntireSource() {
		// TODO: implement test
	}
	
	public void testGetPLExport() {
		// TODO: implement test
	}
	
	public void testSetPLExport() {
		// TODO: implement test
	}
	
	public void testIsModified() {
		// TODO: implement test
	}
	
	public void testSetModified() {
		// TODO: implement test
	}
}
