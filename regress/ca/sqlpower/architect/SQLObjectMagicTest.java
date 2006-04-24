package regress.ca.sqlpower.architect;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import junit.framework.TestCase;

/**
 * Test the MagicEnabled stuff in SQLObject
 */
public class SQLObjectMagicTest extends TestCase {

	SQLObject parent = new SQLTable();
	SQLObject child = new SQLTable.Folder<SQLColumn>(SQLTable.Folder.COLUMNS, false);
	
	@Override
	protected void setUp() throws Exception {
		parent.addChild(child);
	}
	
	public void testOneLevel() {
		assertTrue(child.isMagicEnabled());
		child.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(true);
		assertTrue(child.isMagicEnabled());
		
	}
	
	public void testMultipleDisables() {
		assertTrue(child.isMagicEnabled());
		child.setMagicEnabled(false);
		child.setMagicEnabled(false);	// twice in a row, just to be sure :-)
		child.setMagicEnabled(true);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(true);
		assertTrue(child.isMagicEnabled());
	}
	
	public void testParentChild() {
		assertTrue(child.isMagicEnabled());
		parent.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(true);
		assertFalse(child.isMagicEnabled()); // Because parent still magic-enabled
		parent.setMagicEnabled(true);
		try {
			child.setMagicEnabled(true);		// Should object
		} catch (Throwable e) { 
			System.out.println("Caught expected: " + e);
		}
		assertTrue(child.isMagicEnabled());
		child.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());	// finally
	}
	
}
