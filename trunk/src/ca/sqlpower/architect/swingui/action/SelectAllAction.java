package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PrintPanel;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class SelectAllAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(PrintAction.class);

	/**
	 * The PlayPen instance that this Action operates on.
	 */
	protected PlayPen pp;

	public SelectAllAction() {
		super("Select All");
		putValue(SHORT_DESCRIPTION, "Select All");
	}

	public void actionPerformed(ActionEvent evt) {
		pp.selectAll();
	}
	
	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}

}
