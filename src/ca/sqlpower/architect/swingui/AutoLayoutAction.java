package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

public class AutoLayoutAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(AutoLayoutAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public AutoLayoutAction() {
		super("Automatic Layout");
		putValue(SHORT_DESCRIPTION, "Automatic Layout");
	}

	public void actionPerformed(ActionEvent evt) {
		JOptionPane.showMessageDialog(null, "Auto Layout isn't supported yet!");
	}
}
