package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class CompareDMAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(CompareDMAction.class);

	protected ArchitectFrame architectFrame;

	public CompareDMAction() {		
		super("Compare DM...",
				  ASUtils.createIcon("CompareDatabases",
										"Compare DM",
										ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		architectFrame = ArchitectFrame.getMainInstance();
		putValue(SHORT_DESCRIPTION, "Compare Data Models");
	}

	public void actionPerformed(ActionEvent e) {
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "Compare Data Models");
		JPanel cp = new JPanel(new BorderLayout(12,12));
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		
		final CompareDMPanel compareDMPanel = new CompareDMPanel(architectFrame.project);
		cp.add(compareDMPanel, BorderLayout.CENTER);

//		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel buttonPanel = compareDMPanel.getButtonPanel();
	
		
		
		JButton okButton = new JButton(compareDMPanel.getStartCompareAction());
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
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

}
