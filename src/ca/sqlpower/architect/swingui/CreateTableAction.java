package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import ca.sqlpower.architect.*;
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
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "New Table");
	}

	public void actionPerformed(ActionEvent evt) {
		SQLTable t = new SQLTable();
		t.initFolders(true);
		t.setTableName("New_Table");
		TablePane tp = new TablePane(t, pp.getFontRenderContext());
		pp.addFloating(tp);
	}
	
	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
