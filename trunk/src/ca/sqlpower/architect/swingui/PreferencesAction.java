package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

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
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "User Preferences");
		
		JPanel cp = new JPanel(new BorderLayout(12,12));
		JTabbedPane tp = new JTabbedPane();
		cp.add(tp, BorderLayout.CENTER);
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

		final PreferencesPanel prefPanel = new PreferencesPanel(af.getUserSettings());
		tp.add("General", prefPanel);

		final JDBCDriverPanel jdbcPanel = new JDBCDriverPanel(af.getUserSettings());
		tp.add("JDBC Drivers", jdbcPanel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					prefPanel.applyChanges();
					jdbcPanel.applyChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					prefPanel.discardChanges();
					jdbcPanel.discardChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(cancelButton);
		
		cp.add(buttonPanel, BorderLayout.SOUTH);
		d.setContentPane(cp);
		d.pack();
		d.setVisible(true);
	}

	public void setArchitectFrame(ArchitectFrame af) {
		this.af = af;
	}
}
