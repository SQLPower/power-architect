package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.AboutPanel;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class AboutAction extends AbstractAction {
	
	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public AboutAction() {
		super("About...", ASUtils.createJLFIcon( "general/Information",
                        "Information", 
                        ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
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
			
		Action okAction = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
					aboutPanel.applyChanges();
					d.setVisible(false);
			}
		};
		okAction.putValue(Action.NAME, "OK");
		JDefaultButton okButton = new JDefaultButton(okAction);
		buttonPanel.add(okButton);
			
		cp.add(buttonPanel, BorderLayout.SOUTH);
		ArchitectPanelBuilder.makeJDialogCancellable(
				d, new CommonCloseAction(d));
		d.getRootPane().setDefaultButton(okButton);
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);
		
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
