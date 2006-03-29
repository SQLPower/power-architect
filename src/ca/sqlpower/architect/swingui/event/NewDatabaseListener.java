package ca.sqlpower.architect.swingui.event;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.DBCSPanel;
import ca.sqlpower.architect.swingui.JDefaultButton;

/*
 * When a new database connection has been established, this listener
 * kicks in to add it to the dropdown list.
 */

public class NewDatabaseListener implements ActionListener {

	private ArchitectFrame frame;
	private String title;
	private JComboBox comboBox;
	
	public NewDatabaseListener(ArchitectFrame frame, String title, JComboBox comboBox) {
		super();
		this.frame = frame;
		this.title = title;
		this.comboBox = comboBox;
	}

	public void actionPerformed(ActionEvent e) {
		
		// ArchitectPanelBuilder cannot handle this because of the
		// wizard-style buttons (right-justified FlowLayout).
		
		final JDialog d = new JDialog(frame,title );
		JPanel plr = new JPanel(new BorderLayout(12,12));
		plr.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		final DBCSPanel dbcsPanel = new DBCSPanel();
		dbcsPanel.setDbcs(new ArchitectDataSource());
		plr.add(dbcsPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JDefaultButton okButton = new JDefaultButton(ArchitectPanelBuilder.OK_BUTTON_LABEL);
		okButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent evt) {
				dbcsPanel.applyChanges();
				ArchitectDataSource dbcs = dbcsPanel.getDbcs();
				
				if ( comboBox != null ) {
					comboBox.addItem(dbcs);
					comboBox.setSelectedItem(dbcs);
				}
							
				frame.getUserSettings().getPlDotIni().addDataSource(dbcs);
				d.setVisible(false);
			}
		});
		
		buttonPanel.add(okButton);
		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				dbcsPanel.discardChanges();
				d.setVisible(false);
			}
		};
		JButton cancelButton = new JButton(cancelAction);
		ArchitectPanelBuilder.makeJDialogCancellable(d, cancelAction);
	
		buttonPanel.add(cancelButton);
		plr.add(buttonPanel, BorderLayout.SOUTH);
		d.getRootPane().setDefaultButton(okButton);
		d.setContentPane(plr);
		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);
	}
	
}
