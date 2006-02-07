package regress.ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;
import ca.sqlpower.architect.swingui.CompareDMPanel;

public class TestCompareDMPanel extends JFCTestCase {

	CompareDMPanel panel;
	Robot robot;

	Component sourcePhysicalRadio = null;
	Component sourceDatabaseDropdown = null;
	Component sourceNewConnButton = null;
	Component sourceCalatalogDropdown = null;
	Component sourceSchemaDropdown = null;
	Component sourcePlayPenRadio = null;

	protected void setUp() throws Exception {
		super.setUp();
		
		setHelper(new JFCTestHelper());
		panel = new CompareDMPanel();
		robot = new Robot();
		
		Component comps[] = ((Container) panel.getComponent(0)).getComponents();
		for (int i = 0; i < comps.length; i++) {
			if ("sourcePhysicalRadio".equals(comps[i].getName())) {
				sourcePhysicalRadio = comps[i];			
			} else if ("sourceDatabaseDropdown".equals(comps[i].getName())) {
				sourceDatabaseDropdown = comps[i];			
			} else if ("sourceNewConnButton".equals(comps[i].getName())) {
				sourceNewConnButton = comps[i];				
			} else if ("sourceCalatalogDropdown".equals(comps[i].getName())) {
				sourceCalatalogDropdown = comps[i];				
			} else if ("sourceSchemaDropdown".equals(comps[i].getName())) {
				sourceSchemaDropdown = comps[i];				
			} else if ("sourcePlayPenRadio".equals(comps[i].getName())) {
				sourcePlayPenRadio = comps[i];				
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
		assertNotNull("Missing component", sourceCalatalogDropdown);
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
		assertFalse(sourceCalatalogDropdown.isEnabled());
		assertFalse(sourceSchemaDropdown.isEnabled());

		flushAWT();

		Point p = sourcePhysicalRadio.getLocationOnScreen();
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);

		flushAWT();
		
		assertTrue(sourceDatabaseDropdown.isEnabled());
		assertTrue(sourceNewConnButton.isEnabled());
		assertTrue(sourceCalatalogDropdown.isEnabled());
		assertTrue(sourceSchemaDropdown.isEnabled());
		
		frame.dispose();
	}
	
	public void testDisableSourceDatabaseComponents() {
		JFrame frame = new JFrame();		
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
		assertFalse(sourceDatabaseDropdown.isEnabled());
		assertFalse(sourceNewConnButton.isEnabled());
		assertFalse(sourceCalatalogDropdown.isEnabled());
		assertFalse(sourceSchemaDropdown.isEnabled());

		Point p = sourcePlayPenRadio.getLocationOnScreen();
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		
		assertFalse(sourceDatabaseDropdown.isEnabled());
		assertFalse(sourceNewConnButton.isEnabled());
		assertFalse(sourceCalatalogDropdown.isEnabled());
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
}
