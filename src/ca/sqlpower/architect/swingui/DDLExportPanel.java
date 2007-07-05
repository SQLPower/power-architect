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

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.swingui.ASUtils.LabelValueBean;


public class DDLExportPanel implements ArchitectPanel {
	private static final Logger logger = Logger.getLogger(DDLExportPanel.class);

    private JPanel panel = new JPanel();
    
	private final ArchitectSwingSession session;

    private JComboBox targetDB;
    private JButton newTargetDB;
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
		GenericDDLGenerator ddlg = session.getDDLGenerator();		
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
		Vector<LabelValueBean> ddlTypes =DDLUtils.getDDLTypes();
        panelProperties.add(dbType = new JComboBox(ddlTypes));
		LabelValueBean unknownGenerator = ASUtils.lvb("Unknown Generator", ddlg.getClass());
		dbType.addItem(unknownGenerator);
		dbType.setSelectedItem(unknownGenerator);
		for (LabelValueBean lvb : ddlTypes) {
            if (ddlg.getClass() == lvb.getValue() && lvb != unknownGenerator) {
                dbType.setSelectedItem(lvb);
            }
        }
		if (dbType.getSelectedItem() != unknownGenerator) {
            // remove the unknown generator if we have a known generator
            dbType.removeItem(unknownGenerator);
        }
		
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
		Class selectedGeneratorClass = null;
		try {
			selectedGeneratorClass = (Class) ((ASUtils.LabelValueBean) dbType.getSelectedItem()).getValue();
			GenericDDLGenerator newGen = (GenericDDLGenerator) selectedGeneratorClass.newInstance();
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
			JOptionPane.showMessageDialog(panel, message, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// ------------------------ Architect Panel Stuff -------------------------
	public boolean applyChanges() {
		GenericDDLGenerator ddlg = session.getDDLGenerator();
		Class selectedGeneratorClass = (Class) ((ASUtils.LabelValueBean) dbType.getSelectedItem()).getValue();
		if (ddlg.getClass() != selectedGeneratorClass) {
			try {
				ddlg = (GenericDDLGenerator) selectedGeneratorClass.newInstance();
				session.setDDLGenerator(ddlg);
			} catch (Exception ex) {
				logger.error("Problem creating user-selected DDL generator", ex);
				throw new RuntimeException("Couldn't create a DDL generator of the selected type");
			}
		}
		if (selectedGeneratorClass == GenericDDLGenerator.class) {
			ddlg.setAllowConnection(true);
			ArchitectDataSource dbcs = (ArchitectDataSource)targetDB.getSelectedItem();
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
    
    public ArchitectDataSource getTargetDB(){
        return (ArchitectDataSource)targetDB.getSelectedItem();
    }
}
