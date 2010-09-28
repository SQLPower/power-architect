package ca.sqlpower.architect.swingui.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;

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

		SQLDatabase database = new SQLDatabase((ArchitectDataSource) (databaseDropdown
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
			JOptionPane.showMessageDialog(panel,
					"Database Connection Erorr", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
