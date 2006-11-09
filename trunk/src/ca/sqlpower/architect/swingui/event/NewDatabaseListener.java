package ca.sqlpower.architect.swingui.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JDialog;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.DBCSPanel;
import ca.sqlpower.architect.swingui.action.DBCSOkAction;

/**
 * When a new database connection has been established, this listener
 * kicks in to add it to the dropdown list.
 */
public class NewDatabaseListener implements ActionListener {

	private ArchitectFrame frame;
	private String title;
	
	public NewDatabaseListener(ArchitectFrame frame, String title, JComboBox comboBox) {
		super();
		this.frame = frame;
		this.title = title;
	}

	public void actionPerformed(ActionEvent e) {
		
		final DBCSPanel dbcsPanel = new DBCSPanel();
		
		dbcsPanel.setDbcs(new ArchitectDataSource());

		DBCSOkAction okButton = new DBCSOkAction(dbcsPanel,true);
		
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
