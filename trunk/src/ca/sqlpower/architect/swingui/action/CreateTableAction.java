package ca.sqlpower.architect.swingui.action;

import java.awt.event.*;
import javax.swing.*;

import ca.sqlpower.architect.*;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;

import org.apache.log4j.Logger;

public class CreateTableAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(CreateTableAction.class);

	/**
	 * The PlayPen instance that this Action operates on.
	 */
	protected PlayPen pp;

	public CreateTableAction() {
		super("New Table",
			  ASUtils.createIcon("NewTable",
								 "New Table",
								 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "New Table");
	}

	public void actionPerformed(ActionEvent evt) {
		SQLTable t = new SQLTable();
		try {
			t.initFolders(true);
		} catch (ArchitectException e) {
			logger.error("Couldn't add folder to table \""+t.getName()+"\"", e);
			JOptionPane.showMessageDialog(null, "Failed to add folder to table:\n"+e.getMessage());
		}
		t.setName("New_Table");
		TablePane tp = new TablePane(t, pp);
		pp.addFloating(tp);
		pp.setMouseMode(PlayPen.MouseModeType.CREATING_TABLE);
	}
	
	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
