package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import java.util.List;
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
		super("New Column",
			  ASUtils.createIcon("NewColumn",
								 "New Column",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "New Column");
	}

	public void actionPerformed(ActionEvent evt) {
		List selection = pp.getSelectedItems();
		if (selection.size() < 1) {
			JOptionPane.showMessageDialog(pp, "Select a table (by clicking on it) and try again.");
		} else if (selection.size() > 1) {
			JOptionPane.showMessageDialog(pp, "You have selected multiple items, but you can only edit one at a time.");
		} else if (selection.get(0) instanceof TablePane) {
			TablePane tp = (TablePane) selection.get(0);
			int idx = tp.getSelectedColumnIndex();
			try {
				if (idx < 0) idx = tp.getModel().getColumnsFolder().getChildCount();
			} catch (ArchitectException e) {
				idx = 0;
			}
			tp.getModel().addColumn(idx, new SQLColumn());
		} else {
			JOptionPane.showMessageDialog(pp, "The selected item type is not recognised");
		}
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
