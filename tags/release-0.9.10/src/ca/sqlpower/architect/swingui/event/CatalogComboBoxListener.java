/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.sql.SPDataSource;

/*
 * Updates schema field when there has been a change in the
 * catalog dropdown.
 */
public class CatalogComboBoxListener implements ActionListener {
	
	private JPanel panel;
	private JComboBox databaseDropdown;
	private JComboBox catalogDropdown;
	private JComboBox schemaDropdown;	
	
	
	public CatalogComboBoxListener (JPanel panel,
									JComboBox databaseDropdown, 
									JComboBox catalogDropdown, 
									JComboBox schemaDropdown){
		this.panel = panel;
		this.databaseDropdown = databaseDropdown;
		this.catalogDropdown = catalogDropdown;
		this.schemaDropdown = schemaDropdown;
	}
	public void actionPerformed(ActionEvent e) {

		schemaDropdown.removeAllItems();
		schemaDropdown.setEnabled(false);

		if (databaseDropdown.getSelectedItem() == null) {
			catalogDropdown.setEnabled(false);
			return;
		}

		SQLDatabase database = new SQLDatabase((SPDataSource) (databaseDropdown
				.getSelectedItem()));

		SQLCatalog catalog;
		
		if (database == null || catalogDropdown.getSelectedItem() == null) {
			catalog = null;
			return;
		}

		catalog = (SQLCatalog) catalogDropdown.getSelectedItem();	

		try {
			catalog.populate();
			if (catalog.getChildType() != SQLSchema.class)			
				return;
			for (SQLObject o : (List<SQLObject>) catalog.getChildren()) {
				schemaDropdown.addItem(o);				
			}
			schemaDropdown.setEnabled(true);
		} catch (ArchitectException ex) {
		    ASUtils.showExceptionDialogNoReport(panel, "Database Connect Error", ex);
		}
	}
}
