package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.JDBCDriverPanel;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.PreferencesPanel;

public class PreferencesAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	/**
	 * The ArchitectFrame instance that owns this Action.
	 */
	protected ArchitectFrame af;
	
	public PreferencesAction() {
		super("User Preferences...");
		putValue(SHORT_DESCRIPTION, "User Preferences");
	}

	public void actionPerformed(ActionEvent evt) {
		showPreferencesDialog();
	}

	public void showPreferencesDialog() {
		logger.debug("showPreferencesDialog");
		
		// XXX Can't easily use ArchitectPanelBuilder since this
		// contains a JTabbedPane which is not an ArchitectPanel.
		final JDialog d = new JDialog(af, "User Preferences");
		
		JPanel cp = new JPanel(new BorderLayout(12,12));
		JTabbedPane tp = new JTabbedPane();
		cp.add(tp, BorderLayout.CENTER);
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

		final PreferencesPanel prefPanel = new PreferencesPanel(af.getUserSettings());
		tp.add("General", prefPanel);

		final JDBCDriverPanel jdbcPanel = new JDBCDriverPanel(af.getArchitectSession());
		tp.add("JDBC Drivers", jdbcPanel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JDefaultButton okButton = new JDefaultButton(ArchitectPanelBuilder.OK_BUTTON_LABEL);
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					prefPanel.applyChanges();
					jdbcPanel.applyChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(okButton);
		
		Action cancelAction = new AbstractAction() {
				public void actionPerformed(ActionEvent evt) {
					prefPanel.discardChanges();
					jdbcPanel.discardChanges();
					d.setVisible(false);
				}
		};
		cancelAction.putValue(Action.NAME, ArchitectPanelBuilder.CANCEL_BUTTON_LABEL);
		JButton cancelButton = new JButton(cancelAction);
		buttonPanel.add(cancelButton);
		
		ArchitectPanelBuilder.makeJDialogCancellable(d, cancelAction);
		d.getRootPane().setDefaultButton(okButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);		
	}
	
	public void setArchitectFrame(ArchitectFrame af) {
		this.af = af;
	}
}
