package regress.ca.sqlpower.architect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;
import ca.sqlpower.architect.LogWriter;

public class LogWriterTest extends TestCase {
	public static final String MESSAGE = "0123456789abcdefghijklmnopqrstuvwxyz";
	private LogWriter theWriter;
	private File theFile;
	
	public void setUp() throws Exception {
		theFile = File.createTempFile("test", null);
		theFile.deleteOnExit();
		theWriter = new LogWriter(theFile.getAbsolutePath());
	}
	
	public void testInfo() throws IOException {
		theWriter.info(MESSAGE);
		theWriter.close();
		BufferedReader is = new BufferedReader(new FileReader(theFile));
		String line = is.readLine();
		assertEquals("(1)" + ' ' + MESSAGE, line);
		is.close();
	}
}
