package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;
import java.sql.*;

public class CreateTableAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(CreateTableAction.class);

	/**
	 * The PlayPen instance that this Action operates on.
	 */
	protected PlayPen pp;

	public CreateTableAction() {
		super("Create Table",
			  ASUtils.createIcon("NewTable",
								 "Create Table",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
	}

	public void actionPerformed(ActionEvent evt) {
		SQLTable t = new SQLTable();
		t.initFolders();
		t.setTableName("New_Table");
		TablePane tp = new TablePane(t);
		pp.addFloating(tp);
	}
	
	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
