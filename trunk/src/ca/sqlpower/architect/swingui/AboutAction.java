package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

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
// 								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "About the Power*Architect");
	}

	public void actionPerformed(ActionEvent evt) {
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
		d.setVisible(true);
		
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
