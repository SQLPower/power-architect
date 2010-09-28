package ca.sqlpower.architect.swingui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;

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
									ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Print");
	}

	public void actionPerformed(ActionEvent evt) {
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "Print");
		JPanel cp = new JPanel(new BorderLayout(12,12));
		cp.setBorder(BorderFactory.createMatteBorder(12,12,12,12, cp.getBackground()));
		final PrintPanel printPanel = new PrintPanel(pp);
		cp.add(printPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton okButton = new JButton("Print");
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					printPanel.applyChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					printPanel.discardChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(cancelButton);
		
		cp.add(buttonPanel, BorderLayout.SOUTH);
		
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);
	}
	
	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
