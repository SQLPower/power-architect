package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class EditTableAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public EditTableAction() {
		super("Table Properties...",
			  ASUtils.createIcon("TableProperties",
								 "Table Properties",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Table Properties");
	}

	public void actionPerformed(ActionEvent evt) {
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
										  
			// first tabbed Pane							  
			JPanel cp = new JPanel(new BorderLayout(12,12));
			cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
			final TableEditPanel editPanel = new TableEditPanel(tp.getModel());
			cp.add(editPanel, BorderLayout.CENTER);
			
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
			JButton okButton = new JButton("Ok");
			okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						editPanel.applyChanges();
						d.setVisible(false);
					}
				});
			buttonPanel.add(okButton);
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						editPanel.discardChanges();
						d.setVisible(false);
					}
				});
			buttonPanel.add(cancelButton);
			
			cp.add(buttonPanel, BorderLayout.SOUTH);
			tabbedPane.addTab("TableProperties",cp);
			
			// second tabbed Pane
			JPanel mcp = new JPanel(new BorderLayout(12,12));
			mcp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
			//
			String[] columnNames = {"SourceDB",
                                    "Source Table",
                                    "Source Column",
                                    "Source Column Type",
                                    "Target Column",
									"Target Column Type"};
			SQLTable tTable = (SQLTable)tp.getModel();
			
			try {
				int colnum = tTable.getColumnsFolder().getChildCount();
				for (int i=0;i<=(colnum-1);i++) {
					SQLColumn tCol = (SQLColumn) tTable.getColumn(i);
					String tColName = tCol.getName();
					SQLColumn sCol= tCol.getSourceColumn();
					String sColName = new String();
					StringBuffer sTableName = new StringBuffer();
					if(sCol != null){
						sColName = sCol.getName();
						SQLObject sParent = sCol.getParentTable();
						while (sParent != null) {
							sTableName.append(sParent.getName());
							
						}
					}
				}
			} catch (ArchitectException ae){
				logger.debug( "Column problems"+ae);
			}	
			
			
			JTable mapTable = new JTable();
			JScrollPane scrollpMap = new JScrollPane(mapTable);
            mcp.add(scrollpMap,BorderLayout.NORTH);
			JPanel buttonMapPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
			JButton okMapButton = new JButton("Ok");
			okMapButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						//editPanel.applyChanges();
						//d.setVisible(false);
					}
				});
			buttonMapPanel.add(okMapButton);
			
			JButton cancelMapButton = new JButton("Cancel");
			cancelMapButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						//editPanel.discardChanges();
						//d.setVisible(false);
					}
				});
			buttonMapPanel.add(cancelMapButton);
			mcp.add(buttonMapPanel, BorderLayout.SOUTH);
			tabbedPane.addTab("Column Mappings",mcp);
			
			
			
			
			d.setContentPane(tabbedPane);
			d.pack();
			d.setVisible(true);
			
		} else {
			JOptionPane.showMessageDialog(pp, "The selected item type is not recognised");
		}
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
