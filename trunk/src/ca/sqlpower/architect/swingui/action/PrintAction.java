package ca.sqlpower.architect.swingui.action;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PrintPanel;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class PrintAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(PrintAction.class);

	/**
	 * The PlayPen instance that this Action operates on.
	 */
	protected PlayPen pp;

	public PrintAction() {
		super("Print...",
			  ASUtils.createJLFIcon("general/Print",
									"Print",
									ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Print");
	}

	public void actionPerformed(ActionEvent evt) {
		final PrintPanel printPanel = new PrintPanel(pp);
		
		final JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
				printPanel, 
				ArchitectFrame.getMainInstance(),
				"Print", "Print");
		
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);
	}
	
	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
