/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
