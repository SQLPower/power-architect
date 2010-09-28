package ca.sqlpower.architect;

import java.util.Comparator;
import java.util.Map;

import junit.framework.TestCase;

public class TestArchitectDataSource extends TestCase {

	ArchitectDataSource ds;
	
	protected void setUp() throws Exception {
		super.setUp();
		ds = new ArchitectDataSource();
		ds.setDisplayName("Regression Test");
		ds.setDriverClass("com.does.not.exist");
		ds.setName("test_name");
		ds.setOdbcDsn("fake_odbc_dsn");
		ds.setPass("fake_password");
		ds.setPlDbType("fake_pl_type");
		ds.setPlSchema("my_fake_pl_schema");
		ds.setUrl("jdbc:fake:fake:fake");
		ds.setUser("fake_user");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.hashCode()'
	 */
	public void testHashCode() {
		// cache the old hash code, change a property, and ensure the hash code changes
		String oldPass = ds.getPass();
		int oldCode = ds.hashCode();
		ds.setPass("cows");
		assertFalse(oldCode == ds.hashCode());
		
		// now put the property back, and ensure the hash code goes back to its original value
		ds.setPass(oldPass);
		assertEquals(oldCode, ds.hashCode());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.ArchitectDataSource()'
	 */
	public void testArchitectDataSource() {
		assertNotNull(ds.getPropertiesMap());
		assertNotNull(ds.getPropertyChangeListeners());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.put(String, String)'
	 */
	public void testPut() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.put("test_key", "peek-a-boo!");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("test_key", l.getLastPropertyChange());
		assertEquals("peek-a-boo!", ds.get("test_key"));
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.getPropertiesMap()'
	 */
	public void testGetPropertiesMap() {
		// ensure clients can't get at a mutable reference to the map!
		Map m = ds.getPropertiesMap();
		assertNotNull(m);
		try {
			m.put("won't work", "fooooooey");
			fail("client view of property map must be immutable");
		} catch (UnsupportedOperationException ex) {
			assertTrue(true);
		}
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.equals(Object)'
	 */
	public void testEquals() {
		ArchitectDataSource ds1 = new ArchitectDataSource();
		ArchitectDataSource ds2 = new ArchitectDataSource();
		
		ds1.setDisplayName("Regression Test");
		ds2.setDisplayName("Regression Test");
		ds1.setDriverClass("com.does.not.exist");
		ds2.setDriverClass("com.does.not.exist");
		ds1.setName("test_name");
		ds2.setName("test_name");
		ds1.setOdbcDsn("fake_odbc_dsn");
		ds2.setOdbcDsn("fake_odbc_dsn");
		ds1.setPass("fake_password");
		ds2.setPass("fake_password");
		ds1.setPlDbType("fake_pl_type");
		ds2.setPlDbType("fake_pl_type");
		ds1.setPlSchema("my_fake_pl_schema");
		ds2.setPlSchema("my_fake_pl_schema");
		ds1.setUrl("jdbc:fake:fake:fake");
		ds2.setUrl("jdbc:fake:fake:fake");
		ds1.setUser("fake_user");
		ds2.setUser("fake_user");
		
		assertEquals(ds1, ds2);
		
		// try a known property
		ds2.setDisplayName("x");
		assertFalse(ds1.equals(ds2));

		ds2.setDisplayName(ds1.getDisplayName());
		assertEquals(ds1, ds2);
		
		// try a dynamic property
		ds1.put("cow", "moo");
		assertFalse(ds1.equals(ds2));
	}
	
	public void testEqualsNull() {
		assertFalse("This is really to check null comparison is allowed", ds.equals(null));
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.addPropertyChangeListener(PropertyChangeListener)'
	 */
	public void testAddPropertyChangeListener() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		assertTrue(ds.getPropertyChangeListeners().contains(l));
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.removePropertyChangeListener(PropertyChangeListener)'
	 */
	public void testRemovePropertyChangeListener() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		assertTrue(ds.getPropertyChangeListeners().contains(l));
		ds.removePropertyChangeListener(l);
		assertFalse(ds.getPropertyChangeListeners().contains(l));
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.setName(String)'
	 */
	public void testSetName() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.setName("test");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("name", l.getLastPropertyChange());
		assertEquals("test", ds.getName());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.setDisplayName(String)'
	 */
	public void testSetDisplayName() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.setDisplayName("test");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("name", l.getLastPropertyChange());
		assertEquals("test", ds.getDisplayName());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.setUrl(String)'
	 */
	public void testSetUrl() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.setUrl("test");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("url", l.getLastPropertyChange());
		assertEquals("test", ds.getUrl());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.setDriverClass(String)'
	 */
	public void testSetDriverClass() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.setDriverClass("test");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("driverClass", l.getLastPropertyChange());
		assertEquals("test", ds.getDriverClass());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.setUser(String)'
	 */
	public void testSetUser() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.setUser("test");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("user", l.getLastPropertyChange());
		assertEquals("test", ds.getUser());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.setPass(String)'
	 */
	public void testSetPass() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.setPass("test");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("pass", l.getLastPropertyChange());
		assertEquals("test", ds.getPass());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.setPlSchema(String)'
	 */
	public void testSetPlSchema() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.setPlSchema("test");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("plSchema", l.getLastPropertyChange());
		assertEquals("test", ds.getPlSchema());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.setPlDbType(String)'
	 */
	public void testSetPlDbType() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.setPlDbType("test");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("plDbType", l.getLastPropertyChange());
		assertEquals("test", ds.getPlDbType());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.ArchitectDataSource.setOdbcDsn(String)'
	 */
	public void testSetOdbcDsn() {
		CountingPropertyChangeListener l = new CountingPropertyChangeListener();
		ds.addPropertyChangeListener(l);
		ds.setOdbcDsn("test");
		
		assertEquals(1, l.getPropertyChangeCount());
		assertEquals("odbcDsn", l.getLastPropertyChange());
		assertEquals("test", ds.getOdbcDsn());
	}

	public void testComparator() {
		// set up identical second data source
		ArchitectDataSource ds2 = new ArchitectDataSource();
		for (String key : ds.getPropertiesMap().keySet()) {
			ds2.put(key, ds.get(key));
		}
		
		Comparator<ArchitectDataSource> cmp = new ArchitectDataSource.DefaultComparator();
		assertEquals(0, cmp.compare(ds, ds2));
		
		// test that the display name takes precedence over other properties
		ds2.setDisplayName("a");
		ds2.setUser("z");
		assertTrue(cmp.compare(ds, ds2) > 0);
		
		ds2.setDisplayName("z");
		ds2.setUser("a");
		assertTrue(cmp.compare(ds, ds2) < 0);
		
		// test that comparison bubbles down to user (which is near the end of the comparison order)
		ds2.setDisplayName(ds.getDisplayName());
		ds2.setUser("a");
		assertTrue(cmp.compare(ds, ds2) > 0);

		ds2.setUser("z");
		assertTrue(cmp.compare(ds, ds2) < 0);
	}
}
