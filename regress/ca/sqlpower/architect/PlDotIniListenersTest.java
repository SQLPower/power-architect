package regress.ca.sqlpower.architect;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.DatabaseListChangeEvent;
import ca.sqlpower.architect.DatabaseListChangeListener;
import ca.sqlpower.architect.PlDotIni;
import junit.framework.TestCase;

public class PlDotIniListenersTest extends TestCase {
	PlDotIni pld = new PlDotIni();
	ArchitectDataSource dbcs = new ArchitectDataSource();
	
	@Override
	protected void setUp() throws Exception {
		dbcs.setDisplayName("Goofus");
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.addDataSource(ArchitectDataSource)'
	 * Test it without any listeners.
	 */
	public void testAddDataSource() {
		assertEquals(0, pld.getConnections().size());
		pld.addDataSource(dbcs);
		assertEquals(1, pld.getConnections().size());
		try {
			pld.addDataSource(dbcs);	// should fail!
			fail("Didn't fail to add a second copy!");
		} catch (IllegalArgumentException e) {
			System.out.println("Caught expected " + e);
		}
	}

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.mergeDataSource(ArchitectDataSource)'
	 */
	public void testMergeDataSource() {
		pld.addDataSource(dbcs);
		dbcs.setDriverClass("mock.Driver");
		pld.mergeDataSource(dbcs);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.removeDataSource(ArchitectDataSource)'
	 */
	public void testRemoveDataSource() {
		assertEquals(0, pld.getConnections().size());
		pld.addDataSource(dbcs);
		assertEquals(1, pld.getConnections().size());
		assertSame(dbcs, pld.getConnections().get(0));
		pld.removeDataSource(dbcs);
		assertEquals(0, pld.getConnections().size());
	}
	
	DatabaseListChangeEvent addNotified;
	DatabaseListChangeEvent removeNotified;
	
	DatabaseListChangeListener liszt = new DatabaseListChangeListener() {

		public void databaseAdded(DatabaseListChangeEvent e) {
			addNotified = e;
		}

		public void databaseRemoved(DatabaseListChangeEvent e) {
			removeNotified = e;
		}
	};

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.addListener(DatabaseListChangeListener)'
	 */
	public void testAddListener() {
		pld.addListener(liszt);
		assertNull(addNotified);
		pld.addDataSource(dbcs);
		assertNotNull(addNotified);
		System.out.println(addNotified);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.removeListener(DatabaseListChangeListener)'
	 */
	public void testRemoveListener() {

	}

}
