package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PrintPanel;

public class PrintAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(PrintAction.class);

	public PrintAction(ArchitectSwingSession session) {
		super(session, "Print...", "Print", "printer");
	}

	public void actionPerformed(ActionEvent evt) {
		logger.debug(getValue(SHORT_DESCRIPTION) + ": started");
		
		final PrintPanel printPanel = new PrintPanel(session);
		
		final JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
				printPanel, 
				frame,
				"Print", "Print");
		
		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);
	}
}
