package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.architect.swingui.CompareDMPanel;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class CompareDMAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(CompareDMAction.class);

	protected ArchitectFrame architectFrame;

	public CompareDMAction() {		
		super("Compare DM...",
				  ASUtils.createIcon("CompareDatabases",
										"Compare DM",
										ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		architectFrame = ArchitectFrame.getMainInstance();
		putValue(SHORT_DESCRIPTION, "Compare Data Models");
	}

	public void actionPerformed(ActionEvent e) {
		
		// This can not easily be replaced with ArchitectPanelBuilder
		// because the current CompareDMPanel is not an ArchitectPanel
		// (and has no intention of becoming one, without some work).
		
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "Compare Data Models");
		JPanel cp = new JPanel(new BorderLayout(12,12));
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		
		final CompareDMPanel compareDMPanel = new CompareDMPanel(architectFrame.getProject());
		cp.add(compareDMPanel, BorderLayout.CENTER);

//		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel buttonPanel = compareDMPanel.getButtonPanel();
		
		JButton okButton = new JButton(compareDMPanel.getStartCompareAction());
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton(new CommonCloseAction(d));	
		buttonPanel.add(cancelButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);
		ArchitectPanelBuilder.makeJDialogCancellable(d, cancelButton.getAction());
		d.getRootPane().setDefaultButton(okButton);
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);
	}

}
