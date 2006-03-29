package ca.sqlpower.architect.swingui;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 * A button that can be (and remain as!) the Default button
 * in a JRootPane; works around Java Sun Bug Parade Bug #6199625
 * @see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6199625
 */
public class JDefaultButton extends JButton {
	public JDefaultButton(Action a) {
		super(a);
	}
	public JDefaultButton(String labelText) {
		super(labelText);
	}
	/** 
	 * Work around Java Sun Bug Parade Bug #6199625
	 * @see java.awt.Component#removeNotify()
	 */
	@Override
	public void removeNotify() {
		JRootPane root = SwingUtilities.getRootPane(this);
		JButton defaultButton = root.getDefaultButton();
		super.removeNotify();
		if (defaultButton == this) {
			root.setDefaultButton(this);
		}
	}
}