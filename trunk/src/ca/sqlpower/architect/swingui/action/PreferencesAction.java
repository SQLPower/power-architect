package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectDataSourceTypeEditor;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.PreferencesPanel;

import com.jgoodies.forms.factories.ButtonBarFactory;

public class PreferencesAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	public PreferencesAction(ArchitectSwingSession session) {
        super(session, "User Preferences...", "User Preferences");
	}

	public void actionPerformed(ActionEvent evt) {
		showPreferencesDialog();
	}

	public void showPreferencesDialog() {
		logger.debug("showPreferencesDialog");
		
		// XXX Can't easily use ArchitectPanelBuilder since this
		// contains a JTabbedPane which is not an ArchitectPanel.
		final JDialog d = new JDialog(frame, "User Preferences");
		
		JPanel cp = new JPanel(new BorderLayout(12,12));
		JTabbedPane tp = new JTabbedPane();
		cp.add(tp, BorderLayout.CENTER);
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

		final PreferencesPanel prefPanel = new PreferencesPanel(session);
		tp.add("General", prefPanel);

        final ArchitectDataSourceTypeEditor dsTypeEditor =
            new ArchitectDataSourceTypeEditor(session.getUserSettings().getPlDotIni());
 		tp.add("JDBC Drivers", dsTypeEditor.getPanel());

	
		JDefaultButton okButton = new JDefaultButton(ArchitectPanelBuilder.OK_BUTTON_LABEL);
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					prefPanel.applyChanges();
                    dsTypeEditor.applyChanges();
					d.setVisible(false);
				}
			});
	
		Action cancelAction = new AbstractAction() {
				public void actionPerformed(ActionEvent evt) {
					prefPanel.discardChanges();
                    dsTypeEditor.discardChanges();
					d.setVisible(false);
				}
		};
		cancelAction.putValue(Action.NAME, ArchitectPanelBuilder.CANCEL_BUTTON_LABEL);
		JButton cancelButton = new JButton(cancelAction);

        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);

		ASUtils.makeJDialogCancellable(d, cancelAction);
		d.getRootPane().setDefaultButton(okButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);		
	}
}
