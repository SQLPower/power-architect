package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.AboutPanel;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;

public class AboutAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(AboutAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public AboutAction() {
		super("About...");
// 			  ASUtils.createIcon("TableProperties",
// 								 "Table Properties",
// 								 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "About the Power*Architect");
	}

	public void actionPerformed(ActionEvent evt) {
		// This is one of the few JDIalogs that can not get replaced
		// with a call to ArchitectPanelBuilder, because an About
		// box must have only ONE button...
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "About the Power*Architect");
		JPanel cp = new JPanel(new BorderLayout(12,12));
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		final AboutPanel aboutPanel = new AboutPanel();
		cp.add(aboutPanel, BorderLayout.CENTER);
			
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					aboutPanel.applyChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(okButton);
			
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
