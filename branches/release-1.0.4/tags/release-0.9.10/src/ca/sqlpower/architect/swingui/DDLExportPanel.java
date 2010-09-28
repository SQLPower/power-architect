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
package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanel;


public class DDLExportPanel implements DataEntryPanel {
	private static final Logger logger = Logger.getLogger(DDLExportPanel.class);

    private JPanel panel = new JPanel();
    
	private final ArchitectSwingSession session;

    private JComboBox targetDB;
    private JButton newTargetDB;
    
    /**
     * The selector for which DDL Generator to use.  The contents of this combo box
     * are all of type <tt>Class&lt;? extends DDLGenerator&gt;</tt>.
     */
    private JComboBox dbType;
	
	private JLabel catalogLabel;
	private JTextField catalogField;

	private JLabel schemaLabel;
	private JTextField schemaField;

	public DDLExportPanel(ArchitectSwingSession session) {
		this.session = session;
		setup();
		panel.setVisible(true);
	}

	private void setup() {		
        panel.setLayout(new FormLayout());
        JPanel panelProperties = new JPanel(new FormLayout());
        panelProperties.add(new JLabel("Create in:"));

        panelProperties.add(targetDB = new JComboBox());
        targetDB.setPrototypeDisplayValue("(Target Database)");
        ASUtils.setupTargetDBComboBox(session, targetDB);
        
        newTargetDB = new JButton("Properties");
        newTargetDB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ASUtils.showTargetDbcsDialog(session.getArchitectFrame(), session, targetDB);
                }
            });
        
        panelProperties.add(new JLabel("Generate DDL for Database Type:"));
        DDLGenerator ddlg = session.getDDLGenerator();
		Vector<Class<? extends DDLGenerator>> ddlTypes =
            DDLUtils.getDDLTypes(session.getContext().getPlDotIni());
        if (!ddlTypes.contains(ddlg.getClass())) {
            ddlTypes.add(ddlg.getClass());
        }
        panelProperties.add(dbType = new JComboBox(ddlTypes));
        dbType.setRenderer(new DDLGeneratorListCellRenderer());
		dbType.setSelectedItem(ddlg.getClass());
		dbType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
						setUpCatalogAndSchemaFields();
				}
			});
        
        panelProperties.add(catalogLabel = new JLabel("Target Catalog"));
        panelProperties.add(catalogField = new JTextField(ddlg.getTargetCatalog()));
        panelProperties.add(schemaLabel = new JLabel("Target Schema"));
        panelProperties.add(schemaField = new JTextField(ddlg.getTargetSchema()));
        panel.add(panelProperties);
        panel.add(newTargetDB);
		
		setUpCatalogAndSchemaFields();
        
	}
    
    /**
	 * This method sets up the labels and enabledness of the catalog
	 * and schema text fields.  It should be called every time a new
	 * database type is chosen, and when this panel is set up for the
	 * first time.
	 */
	private void setUpCatalogAndSchemaFields() {
		Class<? extends DDLGenerator> selectedGeneratorClass = null;
		try {
			selectedGeneratorClass = (Class<? extends DDLGenerator>) dbType.getSelectedItem();
			DDLGenerator newGen = selectedGeneratorClass.newInstance();
			if (newGen.getCatalogTerm() != null) {
				catalogLabel.setText(newGen.getCatalogTerm());
				catalogLabel.setEnabled(true);
				catalogField.setEnabled(true);
			} else {
				catalogLabel.setText("(no catalog)");
				catalogLabel.setEnabled(false);
				catalogField.setText(null);
				catalogField.setEnabled(false);
			}
			
			if (newGen.getSchemaTerm() != null) {
				schemaLabel.setText(newGen.getSchemaTerm());
				schemaLabel.setEnabled(true);
				schemaField.setEnabled(true);
			} else {
				schemaLabel.setText("(no schema)");
				schemaLabel.setEnabled(false);
				schemaField.setText(null);
				schemaField.setEnabled(false);
			}
		} catch (Exception ex) {
			String message = "Couldn't create a DDL generator of the selected type";
			if (selectedGeneratorClass != null) {
				message += (":\n"+selectedGeneratorClass.getName());
			}
			logger.error(message, ex);
			ASUtils.showExceptionDialogNoReport(panel, message, ex);
		}
	}

	// ------------------------ Architect Panel Stuff -------------------------
	public boolean applyChanges() {
		DDLGenerator ddlg = session.getDDLGenerator();
		Class<? extends DDLGenerator> selectedGeneratorClass =
            (Class<? extends DDLGenerator>) dbType.getSelectedItem();
		if (ddlg.getClass() != selectedGeneratorClass) {
			try {
				ddlg = selectedGeneratorClass.newInstance();
				session.setDDLGenerator(ddlg);
			} catch (Exception ex) {
				logger.error("Problem creating user-selected DDL generator", ex);
				throw new RuntimeException("Couldn't create a DDL generator of the selected type", ex);
			}
		}
		if (selectedGeneratorClass == GenericDDLGenerator.class) {
			ddlg.setAllowConnection(true);
			SPDataSource dbcs = (SPDataSource)targetDB.getSelectedItem();
			if (dbcs == null
				|| dbcs.getDriverClass() == null
				|| dbcs.getDriverClass().length() == 0) {

				JOptionPane.showMessageDialog
				(panel, "You can't use the Generic JDBC Generator\n"
						+"until you set up the target database connection.");
								
				ASUtils.showTargetDbcsDialog(session.getArchitectFrame(), session, targetDB);
				
				return false;
			}
		} else {
			ddlg.setAllowConnection(false);
		}

		if (catalogField.isEnabled()) {
			if (catalogField.getText() == null || catalogField.getText().trim().length() == 0) {
				JOptionPane.showMessageDialog
				(panel, "Please provide a valid database catalog.");
				return false;
			} else {	
				ddlg.setTargetCatalog(catalogField.getText());
			}
		}
		
		if (schemaField.isEnabled()) {
			if (schemaField.getText() == null || schemaField.getText().trim().length() == 0) {
				JOptionPane.showMessageDialog
				(panel, "Please provide a valid schema name.");
				return false;
			} else {	
				ddlg.setTargetSchema(schemaField.getText());
			}
		}
			
		return true;
		
	}

	public void discardChanges() {
        // nothing to discard
	}

	public JTextField getSchemaField() {
		return schemaField;
	}

	public void setSchemaField(JTextField schemaField) {
		this.schemaField = schemaField;
	}

	public JPanel getPanel() {
		return panel;
	}
    
    public SPDataSource getTargetDB(){
        return (SPDataSource)targetDB.getSelectedItem();
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }
}
