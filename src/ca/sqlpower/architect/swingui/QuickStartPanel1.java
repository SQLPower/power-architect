/*
 * Created on Jun 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import javax.swing.*;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
/**
 * @author jack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QuickStartPanel1 implements WizardPanel {
	private static final Logger logger = Logger.getLogger(WizardPanel.class);
	
	private QuickStartWizard wizard;
	
	public QuickStartPanel1 (QuickStartWizard wizard) {
		this.wizard = wizard;
	}
	
	private Box box; // components laid out in here
	private DBTree dbTree;
	private JScrollPane scrollPane;
	
	public JComponent getPanel() {
		if (box == null) {
			box = Box.createVerticalBox();	    	    

			// add some verbiage at the top
			JLabel verbiage = new JLabel();
			verbiage.setText("<html>Choose the source database and tables.");					
			verbiage.setAlignmentX(Component.CENTER_ALIGNMENT);
			box.add(verbiage);
			box.add(Box.createVerticalStrut(50));

			// static method retrieves database connections
			List databases = QuickStartWizard.getDatabases();
			try {
				dbTree = new DBTree(databases);
			} catch (ArchitectException e) {
				logger.error("problem loading database list",e);
			}
			
			// the list of tables for the selected source
			JScrollPane scrollPane = new JScrollPane(dbTree);
			box.add(scrollPane);
						
			box.add(Box.createVerticalStrut(50));
		}
		return box;		
	}				
			
	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#applyChanges()
	 */
	public boolean applyChanges() {
		// make a list of SQLTable objects and put them into 
		// the wizard
		TreePath [] paths = dbTree.getSelectionPaths();
		if (paths == null || paths.length == 0) {
			JOptionPane.showMessageDialog(getPanel(), "You must select at least one table or view.", "Error", JOptionPane.ERROR_MESSAGE);			
			return false;
		}
		List list = new ArrayList();
		Iterator it = Arrays.asList(paths).iterator();
		while (it.hasNext()) {
			TreePath tp = (TreePath) it.next();
			Object lastObj = tp.getLastPathComponent();
			if (lastObj instanceof SQLTable) {
				logger.error("adding table: " + ((SQLTable)lastObj).getTableName());
				list.add(lastObj);
			}
		}
		wizard.setSourceTables(list);
		return true;
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.architect.swingui.ArchitectPanel#discardChanges()
	 */
	public void discardChanges() {
	    // nothing to throw away
	}

	public String getTitle() {
		return ("Architect Quick Start - Step 1 of 3 - Select Source Tables");
	}
}
