package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class DeleteColumnAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(DeleteColumnAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public DeleteColumnAction(PlayPen pp) {
		super("Delete Column");
		this.pp = pp;
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelection();
		if (invoker instanceof TablePane) {
			TablePane tp = (TablePane) invoker;
			int idx;
			if ( (idx = tp.getSelectedColumnIndex()) >= 0) {  // this was a while() loop, not sure why
				tp.getModel().removeColumn(idx);
			}
		} else {
			JOptionPane.showMessageDialog((JComponent) invoker,
										  "The selected item type is not recognised");
		}
	}
}
