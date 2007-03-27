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
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.PreferencesPanel;

import com.jgoodies.forms.factories.ButtonBarFactory;

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

        final ArchitectDataSourceTypeEditor dsTypeEditor =
            new ArchitectDataSourceTypeEditor(af.getArchitectSession().getUserSettings().getPlDotIni());
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
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);		
	}
	
	public void setArchitectFrame(ArchitectFrame af) {
		this.af = af;
	}
}
