package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class EditColumnAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(EditColumnAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public EditColumnAction(PlayPen pp) {
		super("Edit Column");
		this.pp = pp;
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelectedChild();
		if (invoker instanceof TablePane) {
			TablePane tp = (TablePane) invoker;
			try {
				int idx = tp.getSelectedColumnIndex();
				JFrame editFrame = new JFrame("Edit columns of "+tp.getModel().getName());
				editFrame.setContentPane(new ColumnEditPanel(tp.getModel().getColumn(idx)));
				editFrame.pack();
				editFrame.setVisible(true);
			} catch (ArchitectException e) {
				JOptionPane.showMessageDialog(tp, "Error finding the selected column");
				logger.error("Error finding the selected column", e);
			}
		} else {
			JOptionPane.showMessageDialog((JComponent) invoker,
										  "The selected item type is not recognised");
		}
	}
}
