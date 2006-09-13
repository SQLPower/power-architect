package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;

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
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
	}

	public void actionPerformed(ActionEvent evt) {
		pp.fireCancel();
		SQLTable t = null; 
		try {
            t = new SQLTable();
			t.initFolders(true);
		} catch (ArchitectException e) {
			logger.error("Couldn't add folder to table \""+t.getName()+"\"", e);
			JOptionPane.showMessageDialog(null, "Failed to add folder to table:\n"+e.getMessage());
		}
		t.setName("New_Table");
		
		TablePane tp = new TablePane(t, pp);
		pp.addFloating(tp);
		PlayPen.setMouseMode(PlayPen.MouseModeType.CREATING_TABLE);
	}
	
	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
