package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;
import java.sql.*;

public class InsertColumnAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(InsertColumnAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public InsertColumnAction() {
		super("Insert Column",
			  ASUtils.createIcon("NewColumn",
								 "Insert Column",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelection();
		if (invoker instanceof TablePane) {
			TablePane tp = (TablePane) invoker;
			int idx = tp.getSelectedColumnIndex();
			try {
				if (idx < 0) idx = tp.getModel().getColumnsFolder().getChildCount();
			} catch (ArchitectException e) {
				idx = 0;
			}
			tp.getModel().addColumn(idx, new SQLColumn());
		} else {
			JOptionPane.showMessageDialog((JComponent) invoker,
										  "The selected item type is not recognised");
		}
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
