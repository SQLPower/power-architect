package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class DeleteTableAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(DeleteTableAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public DeleteTableAction(PlayPen pp) {
		super("Delete Table");
		this.pp = pp;
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelectedChild();
		if (invoker instanceof TablePane) {
			TablePane tp = (TablePane) invoker;
			pp.db.removeChild(tp.getModel());
		} else {
			JOptionPane.showMessageDialog((JComponent) invoker,
										  "The selected item type is not recognised");
		}
	}
}
