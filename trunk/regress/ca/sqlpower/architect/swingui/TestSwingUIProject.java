package regress.ca.sqlpower.architect.swingui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
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
		// TODO
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
}
