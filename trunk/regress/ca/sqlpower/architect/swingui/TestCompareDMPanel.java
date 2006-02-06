package regress.ca.sqlpower.architect.swingui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ca.sqlpower.architect.swingui.CompareDMPanel;
import junit.framework.TestCase;

public class TestCompareDMPanel extends TestCase {

	CompareDMPanel panel;
	
	protected void setUp() throws Exception {
		super.setUp();
		panel = new CompareDMPanel();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLayout() {
	}
}
