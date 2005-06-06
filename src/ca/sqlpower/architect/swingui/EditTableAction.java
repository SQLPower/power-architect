package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class EditTableAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;

	/**
	 * The DBTree instance that is associated with this Action.
	 */
	protected DBTree dbt; 

	
	public EditTableAction() {
		super("Table Properties...",
			  ASUtils.createIcon("TableProperties",
								 "Table Properties",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Table Properties");
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
			List selection = pp.getSelectedItems();
			if (selection.size() < 1) {
				JOptionPane.showMessageDialog(pp, "Select a table (by clicking on it) and try again.");
			} else if (selection.size() > 1) {
				JOptionPane.showMessageDialog(pp, "You have selected multiple items, but you can only edit one at a time.");
			} else if (selection.get(0) instanceof TablePane) {
				TablePane tp = (TablePane) selection.get(0);
				
				JTabbedPane tabbedPane = new JTabbedPane();
        	
				final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
											  "Table Properties");
											  
				// first tabbed Pane (table tab)
				JPanel tt = new JPanel(new BorderLayout(12,12));
				tt.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
				final TableEditPanel editPanel = new TableEditPanel(tp.getModel());
				tt.add(editPanel, BorderLayout.CENTER);
				tabbedPane.addTab("TableProperties",tt);
				
        	
				// ok/cancel buttons
				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
				JButton okButton = new JButton("Ok");
				okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							editPanel.applyChanges();
							// XXX: also apply changes on mapping tab
							d.setVisible(false);
						}
					});
				buttonPanel.add(okButton);
				
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							editPanel.discardChanges();
							// XXX: also discard changes on mapping tab
							d.setVisible(false);
						}
					});
				buttonPanel.add(cancelButton);
				
				d.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
				d.getContentPane().add(tabbedPane, BorderLayout.CENTER);
				d.pack();
				d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
				d.setVisible(true);
				
			} else {
				JOptionPane.showMessageDialog(pp, "The selected item type is not recognised");
			}

		} else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {

		} else {
	  		// unknown action command source, do nothing
		}	


	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}

	public void setDBTree(DBTree newDBT) {
		this.dbt = newDBT;
		// do I need to add a selection listener here?
	}

}
