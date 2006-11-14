package ca.sqlpower.architect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import junit.framework.TestCase;

public class PLDotIniTest extends TestCase {

	private static final String FUN_DATASOURCE_NAME = "broomhilda";
	private DataSourceCollection target;

	@Override
	public void setUp() {
		target = new PlDotIni();
	}
	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.read(File)'
	 */
	public void testRead() throws IOException {
		File tmp = makeTempFile();

		target.read(tmp);
		System.out.println("PLDotIniTest::testRead(): Getting here counts as success");
	}
	
	/**
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private File makeTempFile() throws IOException, FileNotFoundException {
		File tmp = File.createTempFile("pl.ini", null);
		
		// The PLDotIni class ONLY reads files with ye anciente MS-DOS line endings...
		PrintWriter out = new PrintWriter(tmp);
		out.print("[random_crap]" + DataSourceCollection.DOS_CR_LF);
		out.print("foo=bar" + DataSourceCollection.DOS_CR_LF);
		out.print("fred=george" + DataSourceCollection.DOS_CR_LF);
		out.print("[Databases_1]" + DataSourceCollection.DOS_CR_LF);
		out.print("Logical=" + FUN_DATASOURCE_NAME + DataSourceCollection.DOS_CR_LF);
		out.print("Type=POSTGRES" + DataSourceCollection.DOS_CR_LF);
		out.print("JDBC Driver Class=org.postgresql.Driver" + DataSourceCollection.DOS_CR_LF);
		out.print("PWD=" + DataSourceCollection.DOS_CR_LF);
		out.print("L Schema Owner=" + DataSourceCollection.DOS_CR_LF);
		out.print("DSN=" + DataSourceCollection.DOS_CR_LF);
		out.print("JDBC URL=jdbc:postgresql://:5432/" + DataSourceCollection.DOS_CR_LF);
		out.flush();
		out.close();
		return tmp;
	}

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.write(OutputStream)'
	 */
	public void testWriteOutputStream() throws IOException {
		File tmp1 = makeTempFile();
		target.read(tmp1);
		File tmp2 = File.createTempFile("pl.out", null);
		target.write(tmp2);
		assertEquals(tmp1.length(), tmp2.length());
		tmp1.deleteOnExit();
		tmp2.deleteOnExit();
	}

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.getDataSource(String)'
	 */
	public void testGetDataSource() throws IOException {
		File tmp = makeTempFile();
		target.read(tmp);
		ArchitectDataSource ds = target.getDataSource(FUN_DATASOURCE_NAME);
		assertNotNull(ds);
		tmp.deleteOnExit();
	}

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.getConnections()'
	 */
	public void testGetConnections() throws IOException {
		File tmp = makeTempFile();
		target.read(tmp);
		List l = target.getConnections();
		assertEquals(1, l.size());
		assertEquals(target.getDataSource(FUN_DATASOURCE_NAME), l.get(0));
		tmp.deleteOnExit();
	}
}
