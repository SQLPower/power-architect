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
import javax.swing.SwingUtilities;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CompareDMPanel;
import ca.sqlpower.architect.swingui.DBCSPanel;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.action.DBCS_OkAction;

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
		
		final DBCSPanel dbcsPanel = new DBCSPanel();
		
		dbcsPanel.setDbcs(new ArchitectDataSource());


		DBCS_OkAction okButton = new DBCS_OkAction(dbcsPanel,true);
		
		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				dbcsPanel.discardChanges();
				
			}
		};
		
		JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
				dbcsPanel,frame,
				title, ArchitectPanelBuilder.OK_BUTTON_LABEL,
				okButton, cancelAction);
		
		okButton.setConnectionDialog(d);
		
		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);
	}
	
}
