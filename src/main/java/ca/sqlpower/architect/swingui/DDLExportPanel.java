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

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.ddl.LiquibaseDDLGenerator;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.swingui.DataEntryPanel;


public class DDLExportPanel implements DataEntryPanel {
	private static final Logger logger = Logger.getLogger(DDLExportPanel.class);

    private JPanel panel = new JPanel();
	private LiquibaseOptionsPanel lbOptions;

	private final ArchitectSwingSession session;

    private JComboBox targetDB;
    private JButton newTargetDB;

    /**
     * The selector for which DDL Generator to use.  The contents of this combo box
     * are all of type <tt>Class&lt;? extends DDLGenerator&gt;</tt>.
     */
    private JComboBox dbType;
	private JCheckBox liquibaseCheckbox;
	private JLabel catalogLabel;
	private JTextField catalogField;

	private JLabel schemaLabel;
	private JTextField schemaField;

	private final DatabaseListChangeListener databaseListChangeListener = new DatabaseListChangeListener() {

        public void databaseRemoved(DatabaseListChangeEvent e) {
            targetDB.removeItem(e.getDataSource());
        }

        public void databaseAdded(DatabaseListChangeEvent e) {
            targetDB.addItem(e.getDataSource());
        }
    };

    /**
     * This is the available data sources that can be forward engineered to.
     */
    private DataSourceCollection<JDBCDataSource> plDotIni;

	public DDLExportPanel(ArchitectSwingSession session) {
		this.session = session;
		setup();
		panel.setVisible(true);
	}

	private void setup() {
        panel.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new FormLayout());
        JPanel panelProperties = new JPanel(new FormLayout());

        panelProperties.add(new JLabel(Messages.getString("DDLExportPanel.createInLabel"))); //$NON-NLS-1$

        panelProperties.add(targetDB = new JComboBox());
        targetDB.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                JDBCDataSource dataSource = (JDBCDataSource) targetDB.getSelectedItem();
                if (dataSource != null) {
                    String generatorClass = dataSource.getParentType().getDDLGeneratorClass();
                    if (generatorClass != null) {
                        try {
                            dbType.setSelectedItem(Class.forName(generatorClass, true, DDLExportPanel.class.getClassLoader()));
                        } catch (ClassNotFoundException ex) {
                            logger.error("Error when finding the DDLGenerator class for the selected database!", ex); //$NON-NLS-1$
                        }
                    }
                }
            }

        });
        targetDB.setPrototypeDisplayValue(Messages.getString("DDLExportPanel.targetDatabase")); //$NON-NLS-1$
        ASUtils.setupTargetDBComboBox(session, targetDB);

        newTargetDB = new JButton(Messages.getString("DDLExportPanel.propertiesButton")); //$NON-NLS-1$
        newTargetDB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ASUtils.showTargetDbcsDialog(session.getArchitectFrame(), session, targetDB);
            }
        });

        panelProperties.add(new JLabel(Messages.getString("DDLExportPanel.generateDDLForDbType"))); //$NON-NLS-1$
        DDLGenerator ddlg = session.getDDLGenerator();
		plDotIni = session.getDataSources();
		plDotIni.addDatabaseListChangeListener(databaseListChangeListener);
        Vector<Class<? extends DDLGenerator>> ddlTypes =
            DDLUtils.getDDLTypes(plDotIni);
        if (!ddlTypes.contains(ddlg.getClass())) {
            ddlTypes.add(ddlg.getClass());
        }
        panelProperties.add(dbType = new JComboBox(ddlTypes));
        dbType.setRenderer(new DDLGeneratorListCellRenderer());
        dbType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUpCatalogAndSchemaFields();
            }
        });

        panelProperties.add(catalogLabel = new JLabel(Messages.getString("DDLExportPanel.targetCatalog"))); //$NON-NLS-1$
        panelProperties.add(catalogField = new JTextField(ddlg.getTargetCatalog()));
        panelProperties.add(schemaLabel = new JLabel(Messages.getString("DDLExportPanel.targetSchema"))); //$NON-NLS-1$
        panelProperties.add(schemaField = new JTextField(ddlg.getTargetSchema()));
        mainPanel.add(panelProperties);
        mainPanel.add(newTargetDB);

        mainPanel.add(liquibaseCheckbox = new JCheckBox(Messages.getString("DDLExportPanel.liqubaseScript"))); //$NON-NLS-1$
		mainPanel.add(new JPanel()); //dummy component to keep the two-column layout


		liquibaseCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setUpCatalogAndSchemaFields();
				dbType.setEnabled(!liquibaseCheckbox.isSelected());
				targetDB.setEnabled(!liquibaseCheckbox.isSelected());
				newTargetDB.setEnabled(!liquibaseCheckbox.isSelected());
				toggleLiquibaseOptions(liquibaseCheckbox.isSelected());
			}
		});
		panel.add(mainPanel, BorderLayout.CENTER);
		lbOptions = new LiquibaseOptionsPanel();
		
		lbOptions.restoreSettings(session.getLiquibaseSettings());
		panel.add(lbOptions.getPanel(), BorderLayout.SOUTH);

		if (ddlg instanceof LiquibaseDDLGenerator) {
			liquibaseCheckbox.setSelected(true);
			lbOptions.getPanel().setVisible(true);
		} else {
			dbType.setSelectedItem(ddlg.getClass());
			lbOptions.getPanel().setVisible(false);
		}

		setUpCatalogAndSchemaFields();
	}

	private void toggleLiquibaseOptions(boolean visible) {
		lbOptions.getPanel().setVisible(visible);
		Window w = SwingUtilities.getWindowAncestor(panel);
		if (w != null) {
			w.pack();
		}
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
			selectedGeneratorClass = getSelectedGeneratorClass();

			DDLGenerator newGen = selectedGeneratorClass.newInstance();
			if (newGen.getCatalogTerm() != null) {
				catalogLabel.setText(newGen.getCatalogTerm());
				catalogLabel.setEnabled(true);
				catalogField.setEnabled(true);
			} else {
				catalogLabel.setText(Messages.getString("DDLExportPanel.noCatalog")); //$NON-NLS-1$
				catalogLabel.setEnabled(false);
				catalogField.setText(null);
				catalogField.setEnabled(false);
			}

			if (newGen.getSchemaTerm() != null) {
				schemaLabel.setText(newGen.getSchemaTerm());
				schemaLabel.setEnabled(true);
				schemaField.setEnabled(true);
			} else {
				schemaLabel.setText(Messages.getString("DDLExportPanel.noSchema")); //$NON-NLS-1$
				schemaLabel.setEnabled(false);
				schemaField.setText(null);
				schemaField.setEnabled(false);
			}
		} catch (Exception ex) {
			String message = Messages.getString("DDLExportPanel.couldNotCreateDdlGenerator"); //$NON-NLS-1$
			if (selectedGeneratorClass != null) {
				message += (":\n"+selectedGeneratorClass.getName()); //$NON-NLS-1$
			}
			logger.error(message, ex);
			ASUtils.showExceptionDialogNoReport(panel, message, ex);
		}
	}

	private Class<? extends DDLGenerator> getSelectedGeneratorClass() {
		if (liquibaseCheckbox.isSelected()) {
			return LiquibaseDDLGenerator.class;
		} else {
			return (Class<? extends DDLGenerator>) dbType.getSelectedItem();
		}
	}

	public DDLGenerator getGenerator() {
		Class<? extends DDLGenerator> genClass = getSelectedGeneratorClass();
		try {
			DDLGenerator gen = genClass.newInstance();
			if (gen instanceof LiquibaseDDLGenerator) {
				((LiquibaseDDLGenerator)gen).applySettings(lbOptions.getLiquibaseSettings());
			}
			return gen;
		} catch (Exception ex) {
			logger.error("Problem creating user-selected DDL generator", ex); //$NON-NLS-1$
			throw new RuntimeException(Messages.getString("DDLExportPanel.couldNotCreateDdlGenerator"), ex); //$NON-NLS-1$
		}
	}

	// ------------------------ Architect Panel Stuff -------------------------
	public boolean applyChanges() {
	    disconnect();
		DDLGenerator ddlg = session.getDDLGenerator();
		Class<? extends DDLGenerator> selectedGeneratorClass = getSelectedGeneratorClass();
		if (ddlg.getClass() != selectedGeneratorClass /*&& selectedGeneratorClass != LiquibaseDDLGenerator.class */) {
			try {
				ddlg = selectedGeneratorClass.newInstance();
				session.setDDLGenerator(ddlg);
			} catch (Exception ex) {
				logger.error("Problem creating user-selected DDL generator", ex); //$NON-NLS-1$
				throw new RuntimeException(Messages.getString("DDLExportPanel.couldNotCreateDdlGenerator"), ex); //$NON-NLS-1$
			}
		}
		session.setLiquibaseSettings(lbOptions.getLiquibaseSettings());
		if (selectedGeneratorClass == GenericDDLGenerator.class) {
			ddlg.setAllowConnection(true);
			// Allow the DDLGenerator to generate scripts even though
			// there is no valid connection available. There may be situations
			// where the user wants to generate, see, and save the script
			// without needing to execute it. This removes the
			// restriction of requiring the user to work online.
			
		} else {
			ddlg.setAllowConnection(false);
		}

		if (catalogField.isEnabled() &&
			catalogField.getText() != null && catalogField.getText().trim().length() > 0) {

			ddlg.setTargetCatalog(catalogField.getText().trim());
		}

		if (schemaField.isEnabled() &&
			schemaField.getText() != null && schemaField.getText().trim().length() > 0) {

			ddlg.setTargetSchema(schemaField.getText().trim());
		}

		return true;

	}

	public void discardChanges() {
        disconnect();
	}

	private void disconnect() {
	    plDotIni.removeDatabaseListChangeListener(databaseListChangeListener);
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

    public JDBCDataSource getTargetDB(){
        return (JDBCDataSource)targetDB.getSelectedItem();
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }
}
