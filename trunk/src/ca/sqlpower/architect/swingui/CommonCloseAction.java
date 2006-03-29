package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

/**
 * Just setVisible(false) and dispose on a JDialog; used in a bunch of places;
 * instantiate once and resuse, to save creating various Actions and Listeners.
 */
public class CommonCloseAction extends AbstractAction {
	private JDialog d;

	public CommonCloseAction(JDialog d) {
		this.d = d;
	}

	public void actionPerformed(ActionEvent e) {
		d.setVisible(false);
		d.dispose();
	}
}