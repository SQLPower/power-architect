package regress.ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRadioButton;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.swingui.CompareDMPanel;

public class TestCompareDMPanel extends JFCTestCase {

	CompareDMPanel panel;
	Robot robot;

	JRadioButton sourcePhysicalRadio = null;
	JComboBox sourceDatabaseDropdown = null;
	Component sourceNewConnButton = null;
	Component sourceCatalogDropdown = null;
	Component sourceSchemaDropdown = null;
	Component sourcePlayPenRadio = null;

	JComboBox targetDatabaseDropdown = null;
	Component targetNewConnButton = null;
	Component targetCatalogDropdown = null;
	Component targetSchemaDropdown = null;
	Component targetPlayPenRadio = null;

	protected void setUp() throws Exception {
		super.setUp();
		
		setHelper(new JFCTestHelper());
		panel = new CompareDMPanel();
		robot = new Robot();
		
		Component comps[] = ((Container) panel.getComponent(0)).getComponents();
		for (int i = 0; i < comps.length; i++) {
			if ("sourcePhysicalRadio".equals(comps[i].getName())) {
				sourcePhysicalRadio = (JRadioButton) comps[i];			
			} else if ("sourceDatabaseDropdown".equals(comps[i].getName())) {
				sourceDatabaseDropdown = (JComboBox) comps[i];			
			} else if ("sourceNewConnButton".equals(comps[i].getName())) {
				sourceNewConnButton = comps[i];				
			} else if ("sourceCatalogDropdown".equals(comps[i].getName())) {
				sourceCatalogDropdown = comps[i];				
			} else if ("sourceSchemaDropdown".equals(comps[i].getName())) {
				sourceSchemaDropdown = comps[i];				
			} else if ("sourcePlayPenRadio".equals(comps[i].getName())) {
				sourcePlayPenRadio = comps[i];				
			} else if ("targetDatabaseDropdown".equals(comps[i].getName())) {
				targetDatabaseDropdown = (JComboBox) comps[i];			
			} else if ("targetNewConnButton".equals(comps[i].getName())) {
				targetNewConnButton = comps[i];				
			} else if ("targetCatalogDropdown".equals(comps[i].getName())) {
				targetCatalogDropdown = comps[i];				
			} else if ("targetSchemaDropdown".equals(comps[i].getName())) {
				targetSchemaDropdown = comps[i];				
			} else if ("targetPlayPenRadio".equals(comps[i].getName())) {
				targetPlayPenRadio = comps[i];				
			}
		}

	}

	protected void tearDown() throws Exception {
		TestHelper.cleanUp(this);
		super.tearDown();
	}
	
	/**
	 * Ensures all components got found in setUp();
	 */
	public void testInitComponents() {		
		assertNotNull("Missing component", sourcePhysicalRadio);
		assertNotNull("Missing component", sourceDatabaseDropdown);
		assertNotNull("Missing component", sourceNewConnButton);
		assertNotNull("Missing component", sourceCatalogDropdown);
		assertNotNull("Missing component", sourceSchemaDropdown);
		assertNotNull("Missing component", sourcePlayPenRadio);
	}

	public void testEnableSourceDatabaseComponents() throws Exception {
		JFrame frame = new JFrame();
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
		assertFalse(sourceDatabaseDropdown.isEnabled());
		assertFalse(sourceNewConnButton.isEnabled());
		assertFalse(sourceCatalogDropdown.isEnabled());
		assertFalse(sourceSchemaDropdown.isEnabled());

		flushAWT();

		Point p = sourcePhysicalRadio.getLocationOnScreen();
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);

		flushAWT();
		
		assertTrue(sourceDatabaseDropdown.isEnabled());
		assertTrue(sourceNewConnButton.isEnabled());
		assertFalse(sourceCatalogDropdown.isEnabled());
		assertFalse(sourceSchemaDropdown.isEnabled());
		
		frame.dispose();
	}
	
	public void testDisableSourceDatabaseComponents() {
		JFrame frame = new JFrame();		
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
		assertFalse(sourceDatabaseDropdown.isEnabled());
		assertFalse(sourceNewConnButton.isEnabled());
		assertFalse(sourceCatalogDropdown.isEnabled());
		assertFalse(sourceSchemaDropdown.isEnabled());

		
		flushAWT();

		// Select the database drop down
		// This is not the point of the test but has to be done
		Point p = sourcePhysicalRadio.getLocationOnScreen();
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);

		flushAWT();
		
		assertTrue(sourceDatabaseDropdown.isEnabled());
		assertTrue(sourceNewConnButton.isEnabled());
		assertFalse(sourceCatalogDropdown.isEnabled());
		assertFalse(sourceSchemaDropdown.isEnabled());
		p = sourcePlayPenRadio.getLocationOnScreen();
		
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		
		flushAWT();
		assertFalse(sourceDatabaseDropdown.isEnabled());
		assertFalse(sourceNewConnButton.isEnabled());
		assertFalse(sourceCatalogDropdown.isEnabled());
		assertFalse(sourceSchemaDropdown.isEnabled());
		
		frame.dispose();
	}
	
	public void testNewSourceConnectionButton() throws Exception {
		JFrame frame = new JFrame();		
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);

		// enables the new source connection button (among other things)
		Point p = sourcePhysicalRadio.getLocationOnScreen();
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		
		// clicks the button (which is now enabled)
		p = sourceNewConnButton.getLocationOnScreen();
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);

		// this (hopefully) forces a wait until the AWT event queue does everything necessary to create the new dialog window
		flushAWT();
		
		JDialog d = panel.getSourceNewConnectionDialog();
		assertNotNull("New source connection button didn't create dialog", d);
		assertTrue("Dialog isn't visible!", d.isVisible());

		frame.dispose();
	}
	
	public void testNewTargetConnectionButton() throws Exception {
		JFrame frame = new JFrame();		
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);

		
		// clicks the button (which is now enabled)
		Point p = targetNewConnButton.getLocationOnScreen();
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);

		// this (hopefully) forces a wait until the AWT event queue does everything necessary to create the new dialog window
		flushAWT();
		
		JDialog d = panel.getTargetNewConnectionDialog();
		assertNotNull("New target connection button didn't create dialog", d);
		assertTrue("Dialog isn't visible!", d.isVisible());

		frame.dispose();
	}
	
	public void testSourceDropDownsWithOnlyCatalog() {
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setDisplayName("Schemaless Database");
		ds.setDriverClass("regress.ca.sqlpower.architect.MockJDBCDriver");
		ds.setUser("fake");
		ds.setPass("fake");
		ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&catalogs=cat1,cat2,cat3");
		sourcePhysicalRadio.setSelected(true);
		
		sourceDatabaseDropdown.addItem(ds);
		sourceDatabaseDropdown.setSelectedItem(ds);
		flushAWT();
		assertFalse(sourceSchemaDropdown.isEnabled());
		assertTrue(sourceCatalogDropdown.isEnabled());
	}
	
	public void testSourceDropDownsWithSchemaAndCatalog() {
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setDisplayName("Schemaless Database");
		ds.setDriverClass("regress.ca.sqlpower.architect.MockJDBCDriver");
		ds.setUser("fake");
		ds.setPass("fake");
		ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=cow_catalog&schemas.cow_catalog=moo_schema,quack_schema&tables.cow_catalog.moo_schema=braaaap,pffft&tables.cow_catalog.quack_schema=duck,goose");
		sourcePhysicalRadio.setSelected(true);
		
		sourceDatabaseDropdown.addItem(ds);
		sourceDatabaseDropdown.setSelectedItem(ds);
		flushAWT();

		assertTrue(sourceCatalogDropdown.isEnabled());
		assertTrue(sourceSchemaDropdown.isEnabled());
	}
	
	public void testSourceDropDownsWithOnlySchema() {
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setDisplayName("Catalogless Database");
		ds.setDriverClass("regress.ca.sqlpower.architect.MockJDBCDriver");
		ds.setUser("fake");
		ds.setPass("fake");
		ds.setUrl("jdbc:mock:dbmd.schemaTerm=Schema&schemas=scheme1,scheme2,scheme3");

		sourcePhysicalRadio.setSelected(true);
		sourceDatabaseDropdown.addItem(ds);
		sourceDatabaseDropdown.setSelectedItem(ds);
		flushAWT();
		assertTrue(sourceSchemaDropdown.isEnabled());
		assertFalse(sourceCatalogDropdown.isEnabled());
	}
	
	public void testTargetDropDownsWithOnlyCatalog() {
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setDisplayName("Schemaless Database");
		ds.setDriverClass("regress.ca.sqlpower.architect.MockJDBCDriver");
		ds.setUser("fake");
		ds.setPass("fake");
		ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&catalogs=cat1,cat2,cat3");
	
		targetDatabaseDropdown.addItem(ds);
		targetDatabaseDropdown.setSelectedItem(ds);
		flushAWT();
		assertFalse(targetSchemaDropdown.isEnabled());
		assertTrue(targetCatalogDropdown.isEnabled());
	}
	
	public void testTargetDropDownsWithSchemaAndCatalog() {
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setDisplayName("Schemaless Database");
		ds.setDriverClass("regress.ca.sqlpower.architect.MockJDBCDriver");
		ds.setUser("fake");
		ds.setPass("fake");
		ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=cow_catalog&schemas.cow_catalog=moo_schema,quack_schema&tables.cow_catalog.moo_schema=braaaap,pffft&tables.cow_catalog.quack_schema=duck,goose");

		targetDatabaseDropdown.addItem(ds);
		targetDatabaseDropdown.setSelectedItem(ds);
		flushAWT();
		assertTrue(targetSchemaDropdown.isEnabled());
		assertTrue(targetCatalogDropdown.isEnabled());
		targetDatabaseDropdown.setSelectedItem(ds);
		flushAWT();
		assertTrue(targetSchemaDropdown.isEnabled());
		assertTrue(targetCatalogDropdown.isEnabled());
	}
	
	public void testTargetDropDownsWithOnlySchema() {
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setDisplayName("Catalogless Database");
		ds.setDriverClass("regress.ca.sqlpower.architect.MockJDBCDriver");
		ds.setUser("fake");
		ds.setPass("fake");
		ds.setUrl("jdbc:mock:dbmd.schemaTerm=Schema&schemas=scheme1,scheme2,scheme3");

		targetDatabaseDropdown.addItem(ds);
		targetDatabaseDropdown.setSelectedItem(ds);
		flushAWT();
		assertTrue(targetSchemaDropdown.isEnabled());
		assertFalse(targetCatalogDropdown.isEnabled());
	}
}
